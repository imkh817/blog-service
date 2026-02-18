package study.blog.global.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import study.blog.global.security.jwt.JwtErrorType;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String errorResponse = generateErrorResponse();

        response.setCharacterEncoding("UTF-8");
        response.setStatus(SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(errorResponse);
    }

    private String generateErrorResponse() {
        return String.format("""
                {
                    "success": false,
                    "message": "%s",
                    "data": null
                }
                """,
                JwtErrorType.ACCESS_DENIED.getMessage()
        );
    }
}
