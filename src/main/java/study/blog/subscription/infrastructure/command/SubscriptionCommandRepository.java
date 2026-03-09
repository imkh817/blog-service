package study.blog.subscription.infrastructure.command;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.subscription.domain.entity.Subscription;

public interface SubscriptionCommandRepository extends JpaRepository<Subscription, Long>, SubscriptionCommandRepositoryCustom {

    int deleteBySubscriberIdAndTargetId(Long subscriberId, Long targetId);
}
