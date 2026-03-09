package study.blog.subscription.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import study.blog.global.common.entity.BaseEntity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class Subscription extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private Long subscriberId;

    private Long targetId;

    public static Subscription createSubscription(Long subscriberId, Long targetId){
        Subscription subscription = new Subscription();
        subscription.subscriberId = subscriberId;
        subscription.targetId = targetId;

        return subscription;
    }
}
