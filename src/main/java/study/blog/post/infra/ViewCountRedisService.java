package study.blog.post.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static study.blog.post.infra.ViewCountRedisKeys.*;

@Service
@RequiredArgsConstructor
public class ViewCountRedisService implements ViewCountService{
    private final StringRedisTemplate redisTemplate;

    public boolean isDuplicated(Long postId, String identifier){
        String key = getDeDuplicationKey(postId, identifier);
        long timeout = getDeDuplicationTTL();
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", timeout, TimeUnit.MINUTES);
        return Boolean.FALSE.equals(isNew);
    }

    public void increaseViewCount(Long postId){
        String key = getViewCountKey(postId);
        redisTemplate.opsForValue().increment(key);
    }
}
