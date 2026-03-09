package study.blog.notification.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.notification.infrastructure.persistence.query.NotificationQueryRepository;
import study.blog.notification.presentation.response.NotificationViewResponse;
import study.blog.notification.presentation.response.UnreadNotificationResponse;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationQueryRepository notificationQueryRepository;

    public List<UnreadNotificationResponse> getUnread(Long memberId) {
        return notificationQueryRepository
                .findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(memberId)
                .stream()
                .map(UnreadNotificationResponse::from)
                .toList();
    }

    public List<NotificationViewResponse> getAll(Long memberId) {
        return notificationQueryRepository
                .findByReceiverIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(NotificationViewResponse::from)
                .toList();
    }
}