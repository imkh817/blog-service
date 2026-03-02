package study.blog.like.postlike.event;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import study.blog.like.postlike.infra.PostLikeRedisKeys;
import study.blog.like.postlike.repository.PostLikeRepository;

import java.time.Duration;

@Order(2)
@Component
@RequiredArgsConstructor
public class PostLikeCountRedisUpdater {

    private final StringRedisTemplate redisTemplate;
    private final PostLikeRepository postLikeRepository;

    //@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void on(PostLikeCountChangedEvent event) {
//        String key = PostLikeRedisKeys.postLikeCount(event.postId());
//
//        // Redis에 키가 없으면 DB의 정확한 count로 초기화
//        // (이 시점에 PostLikeCountDBUpdater가 먼저 실행되어 DB는 이미 업데이트된 상태)
//        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
//            long dbCount = postLikeRepository.countByPostId(event.postId());
//            redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(dbCount));
//        } else {
//            redisTemplate.opsForValue().increment(key, event.delta());
//        }
//
//        redisTemplate.expire(key, Duration.ofDays(1));
//    }
}
