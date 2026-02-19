package study.blog.global.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtTokenProvider 단위 테스트")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String ACCESS_KEY  = "test-access-secret-key-must-be-32-characters!!";
    private static final String REFRESH_KEY = "test-refresh-secret-key-must-be-32-characters!";
    private static final long   ACCESS_EXPIRY_MS  = 900_000L;      // 15분
    private static final long   REFRESH_EXPIRY_MS = 604_800_000L;  // 7일

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "accessKey",           ACCESS_KEY);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshKey",          REFRESH_KEY);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs",   ACCESS_EXPIRY_MS);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpirationInMs", REFRESH_EXPIRY_MS);
    }

    private Authentication authentication(String role) {
        return new UsernamePasswordAuthenticationToken(
                "principal", null, List.of(new SimpleGrantedAuthority(role)));
    }

    // ===== Access Token =====

    @Test
    @DisplayName("Access Token 생성 후 memberId를 정상적으로 추출한다")
    void accessToken_생성_후_memberId_추출() {
        // given
        Long memberId = 1L;
        String token = jwtTokenProvider.generateAccessToken(memberId, authentication("ROLE_USER"));

        // when
        Long extracted = jwtTokenProvider.getMemberIdFromJWT(token, TokenType.ACCESS);

        // then
        assertThat(extracted).isEqualTo(memberId);
    }

    @Test
    @DisplayName("Access Token 생성 후 권한 정보를 정상적으로 추출한다")
    void accessToken_생성_후_권한_추출() {
        // given
        String token = jwtTokenProvider.generateAccessToken(1L, authentication("ROLE_USER"));

        // when
        List<SimpleGrantedAuthority> authorities = jwtTokenProvider.getAuthorities(token, TokenType.ACCESS);

        // then
        assertThat(authorities).containsExactly(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    @DisplayName("만료된 Access Token 파싱 시 ExpiredJwtException이 발생한다")
    void accessToken_만료_시_ExpiredJwtException_발생() {
        // given: 만료 시간을 -1ms 로 설정하여 즉시 만료
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", -1L);
        String expiredToken = jwtTokenProvider.generateAccessToken(1L, authentication("ROLE_USER"));

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.getMemberIdFromJWT(expiredToken, TokenType.ACCESS))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("다른 키로 서명된 Access Token 파싱 시 SignatureException이 발생한다")
    void accessToken_위조_서명_시_SignatureException_발생() {
        // given: 다른 키로 서명된 토큰 생성
        JwtTokenProvider anotherProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(anotherProvider, "accessKey",           "another-access-key-must-be-32-characters!!");
        ReflectionTestUtils.setField(anotherProvider, "refreshKey",          REFRESH_KEY);
        ReflectionTestUtils.setField(anotherProvider, "jwtExpirationInMs",   ACCESS_EXPIRY_MS);
        ReflectionTestUtils.setField(anotherProvider, "refreshExpirationInMs", REFRESH_EXPIRY_MS);

        String foreignToken = anotherProvider.generateAccessToken(1L, authentication("ROLE_USER"));

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.getMemberIdFromJWT(foreignToken, TokenType.ACCESS))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("Access Token의 남은 만료 시간이 양수다")
    void accessToken_남은_만료_시간_양수() {
        // given
        String token = jwtTokenProvider.generateAccessToken(1L, authentication("ROLE_USER"));

        // when
        Duration remaining = jwtTokenProvider.getRemainingExpiry(token, TokenType.ACCESS);

        // then
        assertThat(remaining.toMillis()).isPositive();
    }

    // ===== Refresh Token =====

    @Test
    @DisplayName("Refresh Token 생성 후 memberId를 정상적으로 추출한다")
    void refreshToken_생성_후_memberId_추출() {
        // given
        Long memberId = 2L;
        String token = jwtTokenProvider.generateRefreshToken(memberId);

        // when
        Long extracted = jwtTokenProvider.getMemberIdFromJWT(token, TokenType.REFRESH);

        // then
        assertThat(extracted).isEqualTo(memberId);
    }

    @Test
    @DisplayName("만료된 Refresh Token 파싱 시 ExpiredJwtException이 발생한다")
    void refreshToken_만료_시_ExpiredJwtException_발생() {
        // given
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpirationInMs", -1L);
        String expiredToken = jwtTokenProvider.generateRefreshToken(1L);

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.getMemberIdFromJWT(expiredToken, TokenType.REFRESH))
                .isInstanceOf(ExpiredJwtException.class);
    }

    // ===== 기타 =====

    @Test
    @DisplayName("getRefreshExpirationInMs()가 설정값을 반환한다")
    void getRefreshExpirationInMs_설정값_반환() {
        assertThat(jwtTokenProvider.getRefreshExpirationInMs()).isEqualTo(REFRESH_EXPIRY_MS);
    }
}
