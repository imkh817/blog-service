package study.blog.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.accessKey}")
    private String accessKey;

    @Value("${jwt.refreshKey}")
    private String refreshKey;

    @Value("${jwt.jwtExpirationInMs}")
    private long jwtExpirationInMs;

    @Value("${jwt.refreshExpirationInMs}")
    private long refreshExpirationInMs;

    /**
     * AccessToken 생성
     *
     * @param memberId      인증된 사용자 식별자 (토큰 subject로 사용)
     * @param authentication Spring Security 인증 객체 (권한 정보 추출용)
     */
    public String generateAccessToken(Long memberId, Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date issueDate = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(issueDate.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("authorities", authorities)
                .issuedAt(issueDate)
                .expiration(expiryDate)
                .signWith(getSigningKey(TokenType.ACCESS))
                .compact();
    }

    /**
     * Refresh Token 생성
     *
     * @param memberId 인증된 사용자 식별자 (토큰 subject로 사용)
     */
    public String generateRefreshToken(Long memberId) {
        Date issueDate = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(issueDate.getTime() + refreshExpirationInMs);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(issueDate)
                .expiration(expiryDate)
                .signWith(getSigningKey(TokenType.REFRESH))
                .compact();
    }

    /**
     * 토큰의 남은 유효시간 반환 (블랙리스트 TTL 계산용)
     *
     * @param token      JWT 문자열
     * @param tokenType  ACCESS 또는 REFRESH
     */
    public Duration getRemainingExpiry(String token, TokenType tokenType) {
        Claims claims = parseClaims(token, tokenType);
        long remainingMillis = claims.getExpiration().getTime() - System.currentTimeMillis();
        return Duration.ofMillis(Math.max(remainingMillis, 0));
    }

    public long getRefreshExpirationInMs() {
        return refreshExpirationInMs;
    }

    /**
     * JWT 토큰에서 MemberId(subject) 추출
     *
     * @param token      JWT 문자열
     * @param tokenType  ACCESS 또는 REFRESH
     */
    public Long getMemberIdFromJWT(String token, TokenType tokenType) {
        Claims claims = parseClaims(token, tokenType);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * JWT 토큰에서 권한 정보 추출
     *
     * @param token      JWT 문자열
     * @param tokenType  ACCESS 또는 REFRESH
     */
    public List<SimpleGrantedAuthority> getAuthorities(String token, TokenType tokenType) {
        Claims claims = parseClaims(token, tokenType);
        String authorities = claims.get("authorities", String.class);

        return Arrays.stream(authorities.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * 토큰을 파싱하여 Claims 객체 반환
     *
     * 내부적으로:
     * - 서명 검증
     * - 만료 시간 검증
     */
    private Claims parseClaims(String token, TokenType tokenType) {
        return Jwts.parser()
                .verifyWith(getSigningKey(tokenType))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰 타입에 따라 서명 키 생성
     *
     * ACCESS → accessKey
     * REFRESH → refreshKey
     */
    private SecretKey getSigningKey(TokenType tokenType) {
        String secret = (tokenType == TokenType.ACCESS) ? accessKey : refreshKey;
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
