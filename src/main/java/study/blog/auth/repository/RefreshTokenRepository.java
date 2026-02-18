package study.blog.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import study.blog.auth.entity.RefreshToken;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "RT:";

    private final StringRedisTemplate redisTemplate;

    public void save(RefreshToken refreshToken, Duration ttl) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + refreshToken.getMemberId(),
                refreshToken.getToken(),
                ttl
        );
    }

    public Optional<RefreshToken> findByMemberId(Long memberId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + memberId))
                .map(token -> RefreshToken.restore(memberId, token));
    }

    public void delete(Long memberId) {
        redisTemplate.delete(KEY_PREFIX + memberId);
    }
}
