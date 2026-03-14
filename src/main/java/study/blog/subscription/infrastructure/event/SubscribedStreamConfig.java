package study.blog.subscription.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SubscribedStreamConfig {


    private static final String CONSUMER_NAME = "notification-consumer" + UUID.randomUUID();

    private final StringRedisTemplate redisTemplate;
    private final RedisConnectionFactory redisConnectionFactory;
    private final SubscribedStreamConsumer consumer;

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> subscribedStreamListenerContainer() {
        // 앱 시작 시 Stream과 Consumer Group이 없으면 먼저 생성
        initStreamAndGroup();

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .<String, MapRecord<String, String, String>>builder()
                        // 새 메시지가 없을 때 최대 1초 대기 후 다시 폴링
                        // (1초마다 "새 주문서 왔나?" 확인하는 주기)
                        .pollTimeout(Duration.ofSeconds(1))
                        // Redis의 key/value를 String으로 직렬화/역직렬화
                        .serializer(new StringRedisSerializer())
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);

        container.receive(
                // 어느 그룹의 누가 처리할 것인가 등록
                Consumer.from(SubscribedStreamConsumer.CONSUMER_GROUP, CONSUMER_NAME),
                // 어느 보관함의 어디서부터 읽을 것인가
                // ReadOffset.lastConsumed() = ">" : 아직 아무도 처리하지 않은 새 메시지만 읽음
                StreamOffset.create(SubscribedStreamPublisher.STREAM_KEY, ReadOffset.lastConsumed()),
                // 실제로 처리할 Consumer 등록
                consumer
        );

        // 백그라운드 폴링 스레드 시작
        container.start();
        return container;
    }


    private void initStreamAndGroup() {
        // Stream이 없으면 더미 메시지로 먼저 생성
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(SubscribedStreamPublisher.STREAM_KEY))) {
            redisTemplate.opsForStream().add(
                    SubscribedStreamPublisher.STREAM_KEY,
                    Map.of("_init", "1")
            );
            log.info("Redis Stream 생성: {}", SubscribedStreamPublisher.STREAM_KEY);
        }

        // Consumer Group이 없으면 생성 (이미 있으면 예외 무시)
        try {
            redisTemplate.opsForStream().createGroup(
                    SubscribedStreamPublisher.STREAM_KEY,
                    ReadOffset.from("0"),
                    SubscribedStreamConsumer.CONSUMER_GROUP
            );
            log.info("Consumer Group 생성: {}", SubscribedStreamConsumer.CONSUMER_GROUP);
        } catch (Exception e) {
            log.info("Consumer Group 이미 존재: {}", SubscribedStreamConsumer.CONSUMER_GROUP);
        }
    }
}
