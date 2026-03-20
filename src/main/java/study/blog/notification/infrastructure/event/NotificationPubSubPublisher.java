package study.blog.notification.infrastructure.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import study.blog.notification.domain.event.NotificationSseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPubSubPublisher {

    static final String CHANNEL = "notification:sse";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(NotificationSseEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(CHANNEL, message);
            log.info("SSE 알림 Pub/Sub 발행 - receiverId={}", event.receiverId());
        } catch (JsonProcessingException e) {
            log.error("SSE 알림 직렬화 실패 - receiverId={}", event.receiverId(), e);
        }
    }
}