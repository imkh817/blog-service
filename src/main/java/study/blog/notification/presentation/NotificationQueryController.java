package study.blog.notification.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import study.blog.global.common.dto.ApiResponse;
import study.blog.global.web.resolver.LoginMember;
import study.blog.notification.application.NotificationCommandService;
import study.blog.notification.application.NotificationQueryService;
import study.blog.notification.presentation.response.NotificationViewResponse;
import study.blog.notification.presentation.response.UnreadNotificationResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationQueryController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;

    @GetMapping
    public ApiResponse<List<NotificationViewResponse>> getAll(@LoginMember Long memberId) {
        return ApiResponse.success(notificationQueryService.getAll(memberId));
    }

    @GetMapping("/unread")
    public ApiResponse<List<UnreadNotificationResponse>> getUnread(@LoginMember Long memberId) {
        return ApiResponse.success(notificationQueryService.getUnread(memberId));
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(@LoginMember Long memberId, @PathVariable Long notificationId) {
        notificationCommandService.markAsRead(notificationId, memberId);
        return ApiResponse.success(null);
    }
}