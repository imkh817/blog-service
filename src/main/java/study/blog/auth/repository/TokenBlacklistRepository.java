package study.blog.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class TokenBlacklistRepository {

    private static final String KEY_PREFIX = "BL:";
    private static final String BLACKLISTED_VALUE = "logout";

    private final StringRedisTemplate redisTemplate;

    public void add(String accessToken, Duration remainingTtl) {
        redisTemplate.opsForValue().set(KEY_PREFIX + accessToken, BLACKLISTED_VALUE, remainingTtl);
    }

    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + accessToken));
    }
}
