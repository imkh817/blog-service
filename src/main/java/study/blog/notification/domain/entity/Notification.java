package study.blog.notification.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import study.blog.global.common.entity.BaseEntity;
import study.blog.notification.domain.NotificationType;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;
import static study.blog.notification.domain.NotificationType.SUBSCRIBED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class Notification extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private Long receiverId;

    private Long senderId;

    @Enumerated(value = STRING)
    private NotificationType type;

    private String message;

    private boolean isRead;


    public static Notification createNotification(Long receiverId, Long senderId, NotificationType type, String actorName) {
        Notification notification = new Notification();
        notification.receiverId = receiverId;
        notification.senderId = senderId;
        notification.type = type;
        notification.message = type.createMessage(actorName);
        notification.isRead = false;
        return notification;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
