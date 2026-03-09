package study.blog.subscription.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import study.blog.member.repository.MemberReader;
import study.blog.notification.application.NotificationCommandService;
import study.blog.notification.domain.NotificationType;
import study.blog.notification.domain.event.NotificationSseEvent;
import study.blog.notification.presentation.response.NotificationResponse;
import study.blog.subscription.domain.event.SubscribedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribedEventListener {

    private final ApplicationEventPublisher publisher;
    private final NotificationCommandService notificationCommandService;
    private final MemberReader memberReader;

    /**
     * 구독 이벤트를 처리한다.
     *
     * notification 테이블에 INSERT (오프라인 상태여도 알림 유실 방지)
     *
     * 트랜잭션 정책
     * - AFTER_COMMIT: 구독 트랜잭션이 커밋된 이후에만 실행
     * - REQUIRES_NEW: notification 저장 실패 시에도 구독 데이터에 영향 없음
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SubscribedEvent event) {
        log.info("구독 이벤트 수신 - subscriberId={}, targetId={}", event.subscriberId(), event.targetId());

        String subscriberNickName = memberReader.getNickName(event.subscriberId());

        // DB에 알림 저장
        NotificationResponse notification = notificationCommandService.create(
                event.targetId(),
                event.subscriberId(),
                NotificationType.SUBSCRIBED,
                subscriberNickName
        );

        NotificationSseEvent notificationSseEvent = new NotificationSseEvent(
                notification.notificationId(),
                notification.receiverId(),
                notification.senderId(),
                notification.type(),
                notification.message(),
                notification.isRead(),
                notification.createdAt()
        );

        publisher.publishEvent(notificationSseEvent);
    }
}
