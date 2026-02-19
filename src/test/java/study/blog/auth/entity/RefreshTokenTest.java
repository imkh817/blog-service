package study.blog.auth.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import study.blog.auth.exception.TokenTamperedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RefreshToken 도메인 단위 테스트")
class RefreshTokenTest {

    @Test
    @DisplayName("issue()로 생성하면 memberId와 token을 정상적으로 보유한다")
    void issue_정상_생성() {
        // given
        Long memberId = 1L;
        String token = "original-token";

        // when
        RefreshToken refreshToken = RefreshToken.issue(memberId, token);

        // then
        assertThat(refreshToken.getMemberId()).isEqualTo(memberId);
        assertThat(refreshToken.getToken()).isEqualTo(token);
    }

    @Test
    @DisplayName("일치하는 토큰으로 rotate() 호출 시 새 토큰으로 교체된다")
    void rotate_일치하는_토큰_새_토큰으로_교체() {
        // given
        String originalToken = "original-token";
        String newToken = "new-token";
        RefreshToken refreshToken = RefreshToken.issue(1L, originalToken);

        // when
        refreshToken.rotate(originalToken, newToken);

        // then
        assertThat(refreshToken.getToken()).isEqualTo(newToken);
    }

    @Test
    @DisplayName("일치하지 않는 토큰으로 rotate() 호출 시 TokenTamperedException이 발생한다")
    void rotate_불일치_토큰으로_호출_시_예외_발생() {
        // given
        String originalToken = "original-token";
        String tamperedToken = "tampered-token";
        String newToken = "new-token";
        RefreshToken refreshToken = RefreshToken.issue(1L, originalToken);

        // when & then
        assertThatThrownBy(() -> refreshToken.rotate(tamperedToken, newToken))
                .isInstanceOf(TokenTamperedException.class)
                .hasMessageContaining("토큰이 탈취되었을 가능성");
    }

    @Test
    @DisplayName("rotate() 후 다시 원래 토큰으로 rotate() 시도 시 예외가 발생한다")
    void rotate_교체_후_이전_토큰으로_재시도_시_예외_발생() {
        // given
        String originalToken = "original-token";
        String newToken = "new-token";
        RefreshToken refreshToken = RefreshToken.issue(1L, originalToken);
        refreshToken.rotate(originalToken, newToken);

        // when & then
        assertThatThrownBy(() -> refreshToken.rotate(originalToken, "another-token"))
                .isInstanceOf(TokenTamperedException.class);
    }
}
