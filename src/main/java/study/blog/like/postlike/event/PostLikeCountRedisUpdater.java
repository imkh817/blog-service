package study.blog.like.postlike.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import study.blog.like.postlike.infra.PostLikeRedisKeys;

@Component
@RequiredArgsConstructor
public class PostLikeCountRedisUpdater {
    private final StringRedisTemplate redisTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PostLikeCountChangedEvent event){
        String key = PostLikeRedisKeys.postLikeCount(event.postId());
        redisTemplate.opsForValue().increment(key, event.delta());
    }
}
