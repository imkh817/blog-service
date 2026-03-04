package study.blog.post.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static study.blog.post.infra.ViewCountRedisKeys.getViewCountKey;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisViewCounter implements ViewCountReader {
    private final StringRedisTemplate redisTemplate;

    /**
     * Redis에 누적된 게시글 조회수 증가분을 조회한다.
     *
     * 조회수는 DB에 반영된 값과 Redis에 임시로 누적된 값을 합산하여 계산된다.
     * Redis 장애가 발생하더라도 게시글 조회 자체는 실패하지 않도록
     * 예외를 삼키고 0을 반환하여 DB 값만 사용하도록 fallback 처리한다.
     *
     * @param postId 게시글 ID
     * @return Redis에 누적된 조회수 증가분 (장애 시 0)
     */
    public Long getViewCount(Long postId){
        try{
            String key = getViewCountKey(postId);
            String viewCount = redisTemplate.opsForValue().get(key);
            return viewCount != null ? Long.parseLong(viewCount) : 0L;
        }catch (RedisConnectionFailureException e){
            log.warn("Redis down: viewCount fallback. postId={}", postId, e);
            return 0L;
        }
    }

}
