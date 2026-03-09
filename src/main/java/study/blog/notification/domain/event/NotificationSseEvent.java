package study.blog.notification.domain.event;

import study.blog.notification.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationSseEvent(
        Long notificationId,
        Long receiverId,
        Long senderId,
        NotificationType type,
        String message,
        boolean isRead,
        LocalDateTime createdAt

) {
}
