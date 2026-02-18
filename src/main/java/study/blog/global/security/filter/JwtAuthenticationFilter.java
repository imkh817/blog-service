package study.blog.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import study.blog.auth.repository.TokenBlacklistRepository;
import study.blog.global.security.jwt.JwtTokenProvider;
import study.blog.global.security.jwt.JwtErrorType;
import study.blog.global.web.resolver.LoginMemberId;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.StringUtils.hasText;
import static study.blog.global.security.jwt.TokenType.ACCESS;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractJwtToken(request);

        if (hasText(token)) {
            if (tokenBlacklistRepository.isBlacklisted(token)) {
                sendUnauthorizedResponse(response, JwtErrorType.BLACKLISTED_TOKEN);
                return;
            }

            Long memberId = tokenProvider.getMemberIdFromJWT(token, ACCESS);
            List<SimpleGrantedAuthority> authorities = tokenProvider.getAuthorities(token, ACCESS);

            LoginMemberId loginMemberId = new LoginMemberId(memberId);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(loginMemberId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, JwtErrorType errorType) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format("""
                {
                    "success": false,
                    "message": "%s",
                    "data": null
                }
                """, errorType.getMessage()));
    }

    private String extractJwtToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION))
                .filter(token -> token.length() >= 7 && token.substring(0, 7).equalsIgnoreCase("Bearer "))
                .map(token -> token.substring(7))
                .orElse(null);
    }
}
