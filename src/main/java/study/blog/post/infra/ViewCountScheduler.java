package study.blog.post.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import study.blog.post.repository.PostRepository;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountScheduler {

    private final StringRedisTemplate redisTemplate;
    private final PostRepository postRepository;

    @Transactional
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void flushViewCountToDB(){
        Set<String> keys = redisTemplate.keys("post:view:count:*");

        if(CollectionUtils.isEmpty(keys)) return;

        for(String key : keys){
            String value = redisTemplate.opsForValue().getAndDelete(key);
            if(value == null) continue;

            try{
                Long postId = extractPostId(key);
                long viewCount = Long.parseLong(value);

                postRepository.incrementViewCount(postId, viewCount);
            }catch (Exception e){
                log.error("조회수 DB 반영 실패 - key: {}, count: {} 복원 시도", key, value, e);
                redisTemplate.opsForValue().increment(key, Long.parseLong(value));
            }
        }
    }

    private Long extractPostId(String key) {
        // "post:view:count:42" → 42
        return Long.parseLong(key.split(":")[3]);
    }
}
