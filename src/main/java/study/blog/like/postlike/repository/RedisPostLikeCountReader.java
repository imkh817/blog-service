package study.blog.like.postlike.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import study.blog.like.postlike.infra.PostLikeRedisKeys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisPostLikeCountReader implements PostLikeCountReader{

    private final StringRedisTemplate redisTemplate;
    private final String KEY_PREFIX = "post:like:count:";

    @Override
    public Map<Long, Long> getLikeCounts(List<Long> postIds){
        if(postIds == null || postIds.isEmpty()){
            return Map.of();
        }

        List<String> keys = postIds.stream()
                .map(key -> PostLikeRedisKeys.postLikeCount(key)).toList();

        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        Map<Long, Long> result = new HashMap<>(postIds.size());

        for(int i =0; i<postIds.size(); i++){
            Long postId = postIds.get(i);
            String value = (values == null) ? null : values.get(i);

            long likeCount = 0L;

            if(value != null){
                try{
                    likeCount = Long.parseLong(value);
                }catch (NumberFormatException e){
                    likeCount = 0L;
                }
            }

            result.put(postId, likeCount);
        }

        return result;
    }

    @Override
    public Long getLikeCount(Long postId){
        String likeCount = redisTemplate.opsForValue().get(PostLikeRedisKeys.postLikeCount(postId));
        return likeCount == null ? 0L : Long.parseLong(likeCount);
    }
}
