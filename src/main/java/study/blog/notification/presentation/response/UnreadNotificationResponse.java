package study.blog.notification.presentation.response;

import study.blog.notification.domain.NotificationType;
import study.blog.notification.domain.entity.Notification;

import java.time.LocalDateTime;

public record UnreadNotificationResponse(
        Long id,
        Long senderId,
        NotificationType type,
        String message,
        LocalDateTime createdAt
) {
    public static UnreadNotificationResponse from(Notification notification) {
        return new UnreadNotificationResponse(
                notification.getId(),
                notification.getSenderId(),
                notification.getType(),
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }
}