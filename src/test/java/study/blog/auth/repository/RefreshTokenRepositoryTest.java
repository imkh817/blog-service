package study.blog.auth.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import study.blog.auth.entity.RefreshToken;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenRepository 단위 테스트")
class RefreshTokenRepositoryTest {

    @InjectMocks
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("RefreshToken 저장 시 Redis에 올바른 키와 TTL로 저장된다")
    void save_올바른_키와_TTL로_저장() {
        // given
        Long memberId = 1L;
        String tokenValue = "refresh-token-value";
        Duration ttl = Duration.ofDays(7);
        RefreshToken refreshToken = RefreshToken.issue(memberId, tokenValue);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        refreshTokenRepository.save(refreshToken, ttl);

        // then
        then(valueOperations).should().set("RT:" + memberId, tokenValue, ttl);
    }

    @Test
    @DisplayName("memberId로 조회 시 저장된 RefreshToken을 반환한다")
    void findByMemberId_저장된_토큰_반환() {
        // given
        Long memberId = 1L;
        String tokenValue = "refresh-token-value";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("RT:" + memberId)).willReturn(tokenValue);

        // when
        Optional<RefreshToken> result = refreshTokenRepository.findByMemberId(memberId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getMemberId()).isEqualTo(memberId);
        assertThat(result.get().getToken()).isEqualTo(tokenValue);
    }

    @Test
    @DisplayName("존재하지 않는 memberId로 조회 시 empty Optional을 반환한다")
    void findByMemberId_존재하지_않으면_empty_반환() {
        // given
        Long memberId = 99L;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("RT:" + memberId)).willReturn(null);

        // when
        Optional<RefreshToken> result = refreshTokenRepository.findByMemberId(memberId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("delete() 호출 시 해당 memberId의 키가 삭제된다")
    void delete_올바른_키_삭제() {
        // given
        Long memberId = 1L;

        // when
        refreshTokenRepository.delete(memberId);

        // then
        then(redisTemplate).should().delete("RT:" + memberId);
    }
}
