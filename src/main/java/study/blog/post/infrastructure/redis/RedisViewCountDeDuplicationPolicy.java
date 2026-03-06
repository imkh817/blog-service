package study.blog.post.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import study.blog.post.domain.event.PostViewedEvent;
import study.blog.post.domain.policy.ViewCountDeDuplicationPolicy;

import java.util.concurrent.TimeUnit;

import static study.blog.post.infrastructure.redis.ViewCountRedisKeyGenerator.generateDeDuplicationKey;
import static study.blog.post.infrastructure.redis.ViewCountRedisKeyGenerator.generateDeDuplicationTTL;

@Component
@RequiredArgsConstructor
public class RedisViewCountDeDuplicationPolicy implements ViewCountDeDuplicationPolicy {

    private final StringRedisTemplate redisTemplate;

    /**
     * 조회수 증가 허용 여부를 판단한다.
     *
     * 정책
     * - 동일한 IP(또는 사용자 식별자)가 같은 게시글을 10분 내에 다시 조회하면 조회수는 증가하지 않는다. (중복 조회 방지)
     */
    @Override
    public boolean allow(PostViewedEvent event) {
        String key = generateDeDuplicationKey(event.postId(), event.identifier());
        long timeout = generateDeDuplicationTTL();
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", timeout, TimeUnit.MINUTES);
        return Boolean.TRUE.equals(isNew);
    }
}
