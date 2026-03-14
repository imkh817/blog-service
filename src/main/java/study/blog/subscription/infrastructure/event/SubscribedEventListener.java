package study.blog.subscription.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import study.blog.subscription.domain.event.SubscribedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribedEventListener {

    private final SubscribedStreamPublisher streamPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SubscribedEvent event) {
        log.info("구독 이벤트 수신 → Redis Stream 발행 - subscriberId={}, targetId={}",
                event.subscriberId(), event.targetId());
        streamPublisher.publish(event.subscriberId(), event.targetId());
    }
}
