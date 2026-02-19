package study.blog.auth.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistRepository 단위 테스트")
class TokenBlacklistRepositoryTest {

    @InjectMocks
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("add() 호출 시 토큰이 올바른 키와 TTL로 블랙리스트에 등록된다")
    void add_올바른_키와_TTL로_블랙리스트_등록() {
        // given
        String accessToken = "access-token-value";
        Duration ttl = Duration.ofMinutes(15);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        tokenBlacklistRepository.add(accessToken, ttl);

        // then
        then(valueOperations).should().set("BL:" + accessToken, "logout", ttl);
    }

    @Test
    @DisplayName("블랙리스트에 등록된 토큰은 isBlacklisted()가 true를 반환한다")
    void isBlacklisted_등록된_토큰_true_반환() {
        // given
        String accessToken = "blacklisted-token";

        given(redisTemplate.hasKey("BL:" + accessToken)).willReturn(true);

        // when & then
        assertThat(tokenBlacklistRepository.isBlacklisted(accessToken)).isTrue();
    }

    @Test
    @DisplayName("블랙리스트에 없는 토큰은 isBlacklisted()가 false를 반환한다")
    void isBlacklisted_미등록_토큰_false_반환() {
        // given
        String accessToken = "normal-token";

        given(redisTemplate.hasKey("BL:" + accessToken)).willReturn(false);

        // when & then
        assertThat(tokenBlacklistRepository.isBlacklisted(accessToken)).isFalse();
    }
}
