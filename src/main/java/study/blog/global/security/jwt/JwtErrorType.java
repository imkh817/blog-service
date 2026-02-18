package study.blog.global.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JwtErrorType {
    // JWT 관련 에러
    INVALID_TOKEN("올바르지 않은 JWT 토큰 형식입니다."),
    EXPIRED_TOKEN("만료된 JWT 토큰입니다."),
    UNSUPPORTED_TOKEN("지원하지 않는 JWT 형식입니다."),
    SIGNATURE_MISMATCH("JWT 서명이 유효하지 않습니다."),
    ILLEGAL_TOKEN("JWT 토큰이 비어있거나 잘못되었습니다."),
    BLACKLISTED_TOKEN("로그아웃된 토큰입니다."),

    // 인증 및 인가 관련 에러
    AUTHENTICATION_FAILED("인증에 실패했습니다."),
    ACCESS_DENIED("접근 권한이 없습니다.");


    private final String message;
}
