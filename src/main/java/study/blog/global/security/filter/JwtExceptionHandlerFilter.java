package study.blog.global.security.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import study.blog.global.security.jwt.JwtErrorType;

import java.io.IOException;

public class JwtExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, JwtErrorType.EXPIRED_TOKEN);
        } catch (SignatureException e) {
            sendErrorResponse(response, JwtErrorType.SIGNATURE_MISMATCH);
        } catch (MalformedJwtException e) {
            sendErrorResponse(response, JwtErrorType.INVALID_TOKEN);
        } catch (UnsupportedJwtException e) {
            sendErrorResponse(response, JwtErrorType.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(response, JwtErrorType.ILLEGAL_TOKEN);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, JwtErrorType errorType) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format("""
                {
                    "success": false,
                    "message": "%s",
                    "data": null
                }
                """, errorType.getMessage()));
    }
}
