package study.blog.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import study.blog.member.dto.MemberResponse;
import study.blog.member.entity.Member;
import study.blog.member.exception.MemberNotFoundException;
import study.blog.member.repository.MemberRepository;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public LoginResult login(LoginRequest request) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        MemberUserDetails userDetails = (MemberUserDetails) authenticate.getPrincipal();
        Long memberId = userDetails.getMemberId();

        String accessToken = jwtTokenProvider.generateAccessToken(memberId, authenticate);

        RefreshToken refreshToken = RefreshToken.issue(
                memberId, jwtTokenProvider.generateRefreshToken(memberId));
        refreshTokenRepository.save(refreshToken,
                Duration.ofMillis(jwtTokenProvider.getRefreshExpirationInMs()));

        return new LoginResult(accessToken, refreshToken.getToken(),
                MemberResponse.from(userDetails.getMember()));
    }

    @Transactional
    public ReissueResult reissue(String incomingToken) {
        Long memberId = jwtTokenProvider.getMemberIdFromJWT(incomingToken, TokenType.REFRESH);

        RefreshToken storedToken = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new InvalidRefreshTokenException("로그아웃된 사용자입니다."));

        String newToken = jwtTokenProvider.generateRefreshToken(memberId);

        // 토큰 교체
        try {
            storedToken.rotate(incomingToken, newToken);
        } catch (TokenTamperedException e) {
            // 예외 발생 시 Redis에서 기존 토큰 삭제 처리
            refreshTokenRepository.delete(memberId);
            throw e;
        }

        // 새로운 토큰 저장
        refreshTokenRepository.save(storedToken,
                Duration.ofMillis(jwtTokenProvider.getRefreshExpirationInMs()));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다."));

        MemberUserDetails userDetails = new MemberUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // 새 토큰 발행
        String newAccessToken = jwtTokenProvider.generateAccessToken(memberId, authentication);
        return new ReissueResult(newAccessToken, storedToken.getToken());
    }

    @Transactional
    public void logout(String accessToken) {
        Duration remainingTtl = jwtTokenProvider.getRemainingExpiry(accessToken, TokenType.ACCESS);
        if (!remainingTtl.isZero()) {
            tokenBlacklistRepository.add(accessToken, remainingTtl);
        }

        Long memberId = jwtTokenProvider.getMemberIdFromJWT(accessToken, TokenType.ACCESS);
        refreshTokenRepository.delete(memberId);
    }
}
