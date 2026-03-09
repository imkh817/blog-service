package study.blog.subscription.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import study.blog.subscription.domain.event.SubscribedEvent;

@Component
@RequiredArgsConstructor
public class SubscriptionTracker {
    private final ApplicationEventPublisher publisher;

    public void track(Long subscriberId, Long targetId){
        publisher.publishEvent(new SubscribedEvent(subscriberId, targetId));
    }
}
