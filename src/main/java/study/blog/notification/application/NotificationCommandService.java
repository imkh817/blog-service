package study.blog.notification.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.notification.domain.NotificationType;
import study.blog.notification.domain.entity.Notification;
import study.blog.notification.infrastructure.persistence.query.NotificationQueryRepository;
import study.blog.notification.presentation.response.NotificationResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationCommandService {

    private final NotificationQueryRepository notificationQueryRepository;

    public NotificationResponse create(Long receiverId, Long senderId, NotificationType type, String actorName, String messageId) {
        Notification notification = Notification.createNotification(receiverId, senderId, type, actorName, messageId);
        Notification savedNotification = notificationQueryRepository.save(notification);
        return NotificationResponse.from(savedNotification);
    }

    public void markAsRead(Long notificationId, Long memberId) {
        notificationQueryRepository.findByIdAndReceiverId(notificationId, memberId)
                .ifPresent(Notification::markAsRead);
    }
}