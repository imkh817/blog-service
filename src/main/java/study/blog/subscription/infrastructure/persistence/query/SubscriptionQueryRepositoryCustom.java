package study.blog.subscription.infrastructure.persistence.query;

public interface SubscriptionQueryRepositoryCustom {
    boolean existsSubscription(Long subscriberId, Long targetId);
}
