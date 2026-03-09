package study.blog.notification.presentation.response;

import study.blog.notification.domain.NotificationType;
import study.blog.notification.domain.entity.Notification;

import java.time.LocalDateTime;

public record NotificationViewResponse(
        Long id,
        Long senderId,
        NotificationType type,
        String message,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationViewResponse from(Notification notification) {
        return new NotificationViewResponse(
                notification.getId(),
                notification.getSenderId(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}