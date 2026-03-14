package study.blog.subscription.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import study.blog.subscription.domain.entity.StreamDeadLetter;
import study.blog.subscription.infrastructure.command.StreamDeadLetterRepository;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribedStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    static final String CONSUMER_GROUP = "subscription-notification-group";
    private static final String CONSUMER_NAME = "notification-consumer" + UUID.randomUUID();
    private static final Duration MIN_IDLE_TIME = Duration.ofMinutes(5);
    private static final int MAX_RETRY_COUNT = 3;

    private final SubscriptionNotificationProcessor processor;
    private final StringRedisTemplate redisTemplate;
    private final StreamDeadLetterRepository deadLetterRepository;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        if (isSkippable(message)) {
            ack(message.getId());
            log.info("처리 대상 아닌 메시지 스킵 - messageId={}, value={}", message.getId(), message.getValue());
            return;
        }

        try {
            Long subscriberId = Long.valueOf(message.getValue().get("subscriberId"));
            Long targetId = Long.valueOf(message.getValue().get("targetId"));
            processor.process(subscriberId, targetId);
            ack(message.getId());
        } catch (Exception e) {
            log.error("구독 스트림 메시지 처리 실패 - messageId={}", message.getId(), e);
        }
    }

    /**
     * PEL에 잔류한 미처리 메시지를 1분마다 재처리한다.
     *
     *  MIN_IDLE_TIME 이상 idle한 PEL 메시지를 원자적으로 claim하고 내용을 반환한다.
     *  claim된 메시지는 deliveryCount가 1 증가한다.
     *  다중 인스턴스에서 동시에 호출해도 각 인스턴스가 서로 다른 메시지를 가져간다.
     */
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void reclaimPendingMessages() {
        List<MapRecord<String, Object, Object>> pendingMessages = redisTemplate.opsForStream()
                .claim(SubscribedStreamPublisher.STREAM_KEY,
                        CONSUMER_GROUP,
                        CONSUMER_NAME,
                        MIN_IDLE_TIME);

        if (pendingMessages.isEmpty()) return;

        log.info("PEL 재처리 시작 - pendingCount={}", pendingMessages.size());

        for (MapRecord<String, Object, Object> message : pendingMessages) {
            processReclaimedMessage(message);
        }
    }

    private void processReclaimedMessage(MapRecord<String, Object, Object> message) {
        long deliveryCount = getDeliveryCount(message.getId());

        if (deliveryCount > MAX_RETRY_COUNT) {
            deadLetter(message, deliveryCount);
            return;
        }

        try {
            Long subscriberId = Long.valueOf((String) message.getValue().get("subscriberId"));
            Long targetId = Long.valueOf((String) message.getValue().get("targetId"));
            processor.process(subscriberId, targetId);
            ack(message.getId());
            log.info("PEL 재처리 성공 - messageId={}, deliveryCount={}", message.getId(), deliveryCount);
        } catch (Exception e) {
            log.error("PEL 재처리 실패 - messageId={}, deliveryCount={}", message.getId(), deliveryCount, e);
        }
    }

    /**
     * 최대 재시도 횟수 초과 시 Dead Letter 테이블에 저장하고 PEL에서 제거한다.
     */
    private void deadLetter(MapRecord<String, Object, Object> message, long deliveryCount) {
        try {
            deadLetterRepository.save(StreamDeadLetter.of(
                    SubscribedStreamPublisher.STREAM_KEY,
                    message.getId().getValue(),
                    (String) message.getValue().get("subscriberId"),
                    (String) message.getValue().get("targetId"),
                    (int) deliveryCount
            ));
            ack(message.getId());
            log.error("Dead Letter 저장 완료 - messageId={}, deliveryCount={}", message.getId(), deliveryCount);
        } catch (Exception e) {
            log.error("Dead Letter 저장 실패 - messageId={}", message.getId(), e);
        }
    }

    private long getDeliveryCount(RecordId messageId) {
        return redisTemplate.opsForStream()
                .pending(SubscribedStreamPublisher.STREAM_KEY, CONSUMER_GROUP,
                        org.springframework.data.domain.Range.closed(
                                messageId.getValue(), messageId.getValue()), 1L)
                .stream()
                .findFirst()
                .map(p -> p.getTotalDeliveryCount())
                .orElse(0L);
    }

    private boolean isSkippable(MapRecord<String, String, String> message) {
        return !message.getValue().containsKey("subscriberId");
    }

    private void ack(RecordId recordId) {
        redisTemplate.opsForStream().acknowledge(
                SubscribedStreamPublisher.STREAM_KEY, CONSUMER_GROUP, recordId);
    }
}
