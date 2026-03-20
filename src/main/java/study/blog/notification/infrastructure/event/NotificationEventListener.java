package study.blog.notification.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import study.blog.notification.domain.event.NotificationSseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationPubSubPublisher pubSubPublisher;

    /**
     * notification 저장 트랜잭션 커밋 이후 Pub/Sub 발행
     * - AFTER_COMMIT: DB에 notification이 실제 반영된 이후 발행 보장
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationSseEvent event) {
        log.info("Pub/Sub 발행 - receiverId={}, type={}", event.receiverId(), event.type());
        pubSubPublisher.publish(event);
    }
}
