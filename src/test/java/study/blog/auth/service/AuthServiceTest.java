package study.blog.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import study.blog.auth.dto.LoginRequest;
import study.blog.auth.dto.LoginResult;
import study.blog.auth.dto.ReissueResult;
import study.blog.auth.entity.RefreshToken;
import study.blog.auth.exception.InvalidRefreshTokenException;
import study.blog.auth.exception.TokenTamperedException;
import study.blog.auth.repository.RefreshTokenRepository;
import study.blog.auth.repository.TokenBlacklistRepository;
import study.blog.global.security.jwt.JwtTokenProvider;
import study.blog.global.security.jwt.TokenType;
import study.blog.global.security.principal.MemberUserDetails;
import study.blog.member.entity.Member;
import study.blog.member.repository.MemberRepository;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private MemberRepository memberRepository;

    private Member createMember(Long id) {
        Member member = Member.createMember("test@test.com", "encoded-password", "테스터");
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    // ===== login() =====

    @Test
    @DisplayName("유효한 자격증명으로 로그인 시 AccessToken과 RefreshToken을 반환한다")
    void login_유효한_자격증명_토큰_반환() {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "password123");
        Member member = createMember(1L);
        MemberUserDetails userDetails = new MemberUserDetails(member);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        given(authenticationManager.authenticate(any())).willReturn(auth);
        given(jwtTokenProvider.generateAccessToken(1L, auth)).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("refresh-token");
        given(jwtTokenProvider.getRefreshExpirationInMs()).willReturn(604_800_000L);

        // when
        LoginResult result = authService.login(request);

        // then
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.member().email()).isEqualTo("test@test.com");
        then(refreshTokenRepository).should().save(any(RefreshToken.class), any(Duration.class));
    }

    @Test
    @DisplayName("잘못된 자격증명으로 로그인 시 BadCredentialsException이 전파된다")
    void login_잘못된_자격증명_예외_전파() {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "wrong-password");
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("인증 실패"));

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
        then(refreshTokenRepository).should(never()).save(any(), any());
    }

    // ===== reissue() =====

    @Test
    @DisplayName("유효한 RefreshToken으로 재발급 시 새로운 토큰 쌍을 반환한다")
    void reissue_유효한_토큰_새_토큰_반환() {
        // given
        String incomingToken  = "incoming-refresh-token";
        String newRefreshToken = "new-refresh-token";
        String newAccessToken  = "new-access-token";
        Long memberId = 1L;
        Member member = createMember(memberId);
        RefreshToken storedToken = RefreshToken.issue(memberId, incomingToken);

        given(jwtTokenProvider.getMemberIdFromJWT(incomingToken, TokenType.REFRESH)).willReturn(memberId);
        given(refreshTokenRepository.findByMemberId(memberId)).willReturn(Optional.of(storedToken));
        given(jwtTokenProvider.generateRefreshToken(memberId)).willReturn(newRefreshToken);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(jwtTokenProvider.generateAccessToken(anyLong(), any(Authentication.class))).willReturn(newAccessToken);
        given(jwtTokenProvider.getRefreshExpirationInMs()).willReturn(604_800_000L);

        // when
        ReissueResult result = authService.reissue(incomingToken);

        // then
        assertThat(result.accessToken()).isEqualTo(newAccessToken);
        assertThat(result.newRefreshToken()).isEqualTo(newRefreshToken);
        then(refreshTokenRepository).should().save(any(RefreshToken.class), any(Duration.class));
    }

    @Test
    @DisplayName("Redis에 저장된 RefreshToken이 없으면 InvalidRefreshTokenException이 발생한다")
    void reissue_Redis에_토큰_없음_예외_발생() {
        // given
        String incomingToken = "orphan-token";
        Long memberId = 1L;

        given(jwtTokenProvider.getMemberIdFromJWT(incomingToken, TokenType.REFRESH)).willReturn(memberId);
        given(refreshTokenRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.reissue(incomingToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("로그아웃된 사용자");
    }

    @Test
    @DisplayName("위변조된 RefreshToken으로 재발급 시 TokenTamperedException이 발생하고 Redis에서 삭제된다")
    void reissue_위변조된_토큰_예외_발생_및_Redis_삭제() {
        // given
        String storedTokenValue = "stored-refresh-token";
        String tamperedToken    = "tampered-refresh-token";
        String newRefreshToken  = "new-refresh-token";
        Long memberId = 1L;
        RefreshToken storedToken = RefreshToken.issue(memberId, storedTokenValue);

        given(jwtTokenProvider.getMemberIdFromJWT(tamperedToken, TokenType.REFRESH)).willReturn(memberId);
        given(refreshTokenRepository.findByMemberId(memberId)).willReturn(Optional.of(storedToken));
        given(jwtTokenProvider.generateRefreshToken(memberId)).willReturn(newRefreshToken);

        // when & then
        assertThatThrownBy(() -> authService.reissue(tamperedToken))
                .isInstanceOf(TokenTamperedException.class);
        then(refreshTokenRepository).should().delete(memberId);
    }

    // ===== logout() =====

    @Test
    @DisplayName("로그아웃 시 AccessToken을 블랙리스트에 등록하고 RefreshToken을 삭제한다")
    void logout_블랙리스트_등록_및_토큰_삭제() {
        // given
        String accessToken = "valid-access-token";
        Long memberId = 1L;
        Duration remainingTtl = Duration.ofMinutes(10);

        given(jwtTokenProvider.getRemainingExpiry(accessToken, TokenType.ACCESS)).willReturn(remainingTtl);
        given(jwtTokenProvider.getMemberIdFromJWT(accessToken, TokenType.ACCESS)).willReturn(memberId);

        // when
        authService.logout(accessToken);

        // then
        then(tokenBlacklistRepository).should().add(accessToken, remainingTtl);
        then(refreshTokenRepository).should().delete(memberId);
    }

    @Test
    @DisplayName("로그아웃 시 AccessToken 잔여 만료 시간이 0이면 블랙리스트에 등록하지 않는다")
    void logout_잔여_만료_시간_0이면_블랙리스트_미등록() {
        // given
        String accessToken = "already-expired-token";
        Long memberId = 1L;

        given(jwtTokenProvider.getRemainingExpiry(accessToken, TokenType.ACCESS)).willReturn(Duration.ZERO);
        given(jwtTokenProvider.getMemberIdFromJWT(accessToken, TokenType.ACCESS)).willReturn(memberId);

        // when
        authService.logout(accessToken);

        // then
        then(tokenBlacklistRepository).should(never()).add(any(), any());
        then(refreshTokenRepository).should().delete(memberId);
    }
}
