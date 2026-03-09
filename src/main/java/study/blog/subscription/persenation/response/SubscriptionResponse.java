package study.blog.subscription.persenation.response;

public record SubscriptionResponse(
        Long subscriberId,
        Long targetId
) {
    public static SubscriptionResponse of(Long subscriberId, Long targetId){
        return new SubscriptionResponse(subscriberId, targetId);
    }
}
