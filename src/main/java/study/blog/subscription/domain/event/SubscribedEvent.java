package study.blog.subscription.domain.event;

public record SubscribedEvent(Long subscriberId, Long targetId) {
}
