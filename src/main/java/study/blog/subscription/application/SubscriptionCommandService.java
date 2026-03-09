package study.blog.subscription.application;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.blog.subscription.domain.entity.Subscription;
import study.blog.subscription.domain.exception.DuplicateSubscriptionException;
import study.blog.subscription.domain.exception.SubscriptionNotFoundException;
import study.blog.subscription.infrastructure.command.SubscriptionCommandRepository;
import study.blog.subscription.persenation.response.SubscriptionResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionCommandService {
    private final SubscriptionCommandRepository subscriptionCommandRepository;
    private final SubscriptionTracker subscriptionTracker;

    public SubscriptionResponse subscribe(Long subscriberId, Long targetId){
        try{
            Subscription subscription = Subscription.createSubscription(subscriberId, targetId);
            subscriptionCommandRepository.save(subscription);
        }catch (DataIntegrityViolationException e){
            throw new DuplicateSubscriptionException("이미 구독 중입니다.");
        }

        subscriptionTracker.track(subscriberId, targetId);
        return SubscriptionResponse.of(subscriberId, targetId);
    }

    public SubscriptionResponse unsubscribe(Long subscriberId, Long targetId) {
        int deleted = subscriptionCommandRepository.deleteBySubscriberIdAndTargetId(subscriberId, targetId);
        if (deleted == 0) {
            throw new SubscriptionNotFoundException("구독 중이 아닙니다.");
        }
        return SubscriptionResponse.of(subscriberId, targetId);
    }
}
