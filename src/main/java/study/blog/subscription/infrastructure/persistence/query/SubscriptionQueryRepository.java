package study.blog.subscription.infrastructure.persistence.query;

import org.springframework.data.jpa.repository.JpaRepository;
import study.blog.subscription.domain.entity.Subscription;

public interface SubscriptionQueryRepository extends JpaRepository<Subscription, Long>, SubscriptionQueryRepositoryCustom {
}
