package study.blog.auth.entity;

import study.blog.auth.exception.TokenTamperedException;

public class RefreshToken {

    private final Long memberId;
    private String token;

    private RefreshToken(Long memberId, String token) {
        this.memberId = memberId;
        this.token = token;
    }

    public static RefreshToken issue(Long memberId, String token) {
        return new RefreshToken(memberId, token);
    }

    public static RefreshToken restore(Long memberId, String token) {
        return new RefreshToken(memberId, token);
    }

    /**
     * Refresh Token을 회전(rotating)한다.
     *
     * 현재 저장된 토큰과 전달된 토큰이 일치하는 경우에만
     * 새로운 토큰으로 교체한다.
     *
     * 이는 토큰 재사용(replay attack)을 방지하기 위한 보안 정책이며,
     * 탈취된 이전 토큰으로는 재발급이 불가능하도록 보장한다.
     *
     * @param incomingToken 클라이언트가 보유한 기존 Refresh Token
     * @param newToken 새로 발급된 Refresh Token
     * @throws TokenTamperedException 토큰이 일치하지 않는 경우
     */
    public void rotate(String incomingToken, String newToken) {
        validateMatch(incomingToken);
        this.token = newToken;
    }

    private void validateMatch(String incomingToken) {
        if (!this.token.equals(incomingToken)) {
            throw new TokenTamperedException("토큰이 탈취되었을 가능성이 있습니다. 다시 로그인해주세요.");
        }
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getToken() {
        return token;
    }
}
