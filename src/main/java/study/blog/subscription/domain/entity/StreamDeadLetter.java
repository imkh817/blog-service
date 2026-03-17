package study.blog.subscription.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import study.blog.global.common.entity.BaseEntity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

/**
 * PEL 재처리 최대 횟수를 초과한 메시지를 보관하는 Dead Letter 테이블.
 */
@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class StreamDeadLetter extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String streamKey;
    private String messageId;
    private String subscriberId;
    private String targetId;
    private int deliveryCount;

    public static StreamDeadLetter of(String streamKey, String messageId,
                                      String subscriberId, String targetId,
                                      int deliveryCount) {
        StreamDeadLetter deadLetter = new StreamDeadLetter();
        deadLetter.streamKey = streamKey;
        deadLetter.messageId = messageId;
        deadLetter.subscriberId = subscriberId;
        deadLetter.targetId = targetId;
        deadLetter.deliveryCount = deliveryCount;
        return deadLetter;
    }
}