package study.blog.subscription.persenation;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.blog.global.common.dto.ApiResponse;
import study.blog.global.web.resolver.LoginMember;
import study.blog.subscription.application.SubscriptionCommandService;
import study.blog.subscription.persenation.response.SubscriptionResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscription")
public class SubscriptionCommandController {

    private final SubscriptionCommandService subscriptionCommandService;

    @PostMapping("/{targetId}")
    public ApiResponse<SubscriptionResponse> subscribe(@LoginMember Long subscriberId, @PathVariable Long targetId){
        SubscriptionResponse subscribe = subscriptionCommandService.subscribe(subscriberId, targetId);
        return ApiResponse.success(subscribe);
    }

    @DeleteMapping("/{targetId}")
    public ApiResponse<SubscriptionResponse> unsubscribe(@LoginMember Long subscriberId, @PathVariable Long targetId){
        SubscriptionResponse response = subscriptionCommandService.unsubscribe(subscriberId, targetId);
        return ApiResponse.success(response);
    }

}
