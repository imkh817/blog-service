package study.blog.notification.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import study.blog.notification.application.NotificationSseService;
import study.blog.notification.domain.event.NotificationSseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationSseService notificationSseService;

    /**
     * notification 저장 트랜잭션 커밋 이후 SSE 전송
     * - 트랜잭션 없음: SSE는 DB IO가 아닌 네트워크 IO
     * - AFTER_COMMIT: DB에 notification이 실제 반영된 이후 클라이언트에 전송 보장
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationSseEvent event) {
        log.info("SSE 알림 전송 - receiverId={}, type={}", event.receiverId(), event.type());
        notificationSseService.send(event.receiverId(), event.type(), event);
    }
}