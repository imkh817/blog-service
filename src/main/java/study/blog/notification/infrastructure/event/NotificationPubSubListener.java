package study.blog.notification.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import study.blog.notification.application.NotificationSseService;
import study.blog.notification.domain.event.NotificationSseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPubSubListener implements MessageListener {

    private final NotificationSseService notificationSseService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            NotificationSseEvent event = objectMapper.readValue(message.getBody(), NotificationSseEvent.class);
            log.info("SSE 알림 수신 - receiverId={}, type={}", event.receiverId(), event.type());
            notificationSseService.send(event.receiverId(), event.type(), event);
        } catch (Exception e) {
            log.error("SSE 알림 처리 실패", e);
        }
    }
}