package study.blog.post.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import study.blog.post.application.ViewCountService;

import static study.blog.post.infrastructure.redis.ViewCountRedisKeyGenerator.generateViewCountKey;

@Service
@RequiredArgsConstructor
public class ViewCountRedisService implements ViewCountService {
    private final StringRedisTemplate redisTemplate;

    public void increaseViewCount(Long postId){
        String key = generateViewCountKey(postId);
        redisTemplate.opsForValue().increment(key);
    }

}
