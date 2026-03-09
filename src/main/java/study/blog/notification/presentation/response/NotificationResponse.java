package study.blog.notification.presentation.response;

import study.blog.notification.domain.NotificationType;
import study.blog.notification.domain.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        Long receiverId,
        Long senderId,
        NotificationType type,
        String message,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getReceiverId(),
                notification.getSenderId(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
