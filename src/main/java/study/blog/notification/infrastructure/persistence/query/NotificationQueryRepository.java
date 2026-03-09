package study.blog.notification.infrastructure.persistence.query;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.notification.domain.entity.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationQueryRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(Long receiverId);

    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    Optional<Notification> findByIdAndReceiverId(Long id, Long receiverId);
}