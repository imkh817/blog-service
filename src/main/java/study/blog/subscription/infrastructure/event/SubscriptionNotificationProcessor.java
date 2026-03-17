package study.blog.subscription.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.blog.member.repository.MemberReader;
import study.blog.notification.application.NotificationCommandService;
import study.blog.notification.domain.NotificationType;
import study.blog.notification.domain.event.NotificationSseEvent;
import study.blog.notification.presentation.response.NotificationResponse;


@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionNotificationProcessor {

    private final MemberReader memberReader;
    private final NotificationCommandService notificationCommandService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void process(Long subscriberId, Long targetId, String messageId) {
        log.info("구독 알림 처리 시작 - subscriberId={}, targetId={}", subscriberId, targetId);

        String subscriberNickName = memberReader.getNickName(subscriberId);

        NotificationResponse notification = notificationCommandService.create(
                targetId,
                subscriberId,
                NotificationType.SUBSCRIBED,
                subscriberNickName,
                messageId
        );

        // AFTER_COMMIT 이벤트 — 트랜잭션 커밋 후 SSE 전송 보장
        eventPublisher.publishEvent(new NotificationSseEvent(
                notification.notificationId(),
                notification.receiverId(),
                notification.senderId(),
                notification.type(),
                notification.message(),
                notification.isRead(),
                notification.createdAt()
        ));
    }
}
