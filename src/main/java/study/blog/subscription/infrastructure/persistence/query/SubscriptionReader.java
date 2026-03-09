package study.blog.subscription.infrastructure.persistence.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class SubscriptionReader {
    private final SubscriptionQueryRepository subscriptionQueryRepository;

    public boolean isSubscribe(Long subscriberId, Long targetId){
        return subscriptionQueryRepository.existsSubscription(subscriberId, targetId);
    }
}
