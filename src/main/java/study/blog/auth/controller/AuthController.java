package study.blog.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import study.blog.auth.dto.LoginRequest;
import study.blog.auth.dto.LoginResponse;
import study.blog.auth.dto.LoginResult;
import study.blog.auth.dto.ReissueResponse;
import study.blog.auth.dto.ReissueResult;
import study.blog.auth.exception.InvalidRefreshTokenException;
import study.blog.auth.service.AuthService;
import study.blog.global.common.dto.ApiResponse;
import study.blog.global.security.jwt.JwtTokenProvider;

import java.time.Duration;
import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/api/v1/auth";

    @Value("${jwt.cookieSecure:true}")
    private boolean cookieSecure;

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletResponse response) {
        LoginResult result = authService.login(request);
        setRefreshTokenCookie(response, result.refreshToken());
        return ApiResponse.success(new LoginResponse(result.accessToken(), result.member()));
    }

    @PostMapping("/refresh")
    public ApiResponse<ReissueResponse> reissue(HttpServletRequest request,
                                                 HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        ReissueResult result = authService.reissue(refreshToken);
        setRefreshTokenCookie(response, result.newRefreshToken());
        return ApiResponse.success(new ReissueResponse(result.accessToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authHeader,
                                    HttpServletResponse response) {
        String accessToken = authHeader.substring(7);
        authService.logout(accessToken);
        clearRefreshTokenCookie(response);
        return ApiResponse.success(null);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new InvalidRefreshTokenException("Refresh Token이 존재하지 않습니다.");
        }
        return Arrays.stream(cookies)
                .filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh Token이 존재하지 않습니다."));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(Duration.ofMillis(jwtTokenProvider.getRefreshExpirationInMs()))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
