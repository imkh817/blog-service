package study.blog.subscription.infrastructure.persistence.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static study.blog.subscription.domain.entity.QSubscription.subscription;

@RequiredArgsConstructor
public class SubscriptionQueryRepositoryCustomImpl implements SubscriptionQueryRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsSubscription(Long subscriberId, Long targetId) {
        Integer result = queryFactory
                .selectOne()
                .from(subscription)
                .where(
                        subscriberIdEq(subscriberId),
                        targetIdEq(targetId)
                )
                .fetchFirst();

        return result != null;
    }

    private BooleanExpression subscriberIdEq(Long subscriberId){
        return subscriberId != null ? subscription.subscriberId.eq(subscriberId) : null;
    }

    private BooleanExpression targetIdEq(Long targetId){
        return targetId != null ? subscription.targetId.eq(targetId) : null;
    }
}
