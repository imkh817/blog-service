package study.blog.auth.dto;

public record ReissueResult(
        String accessToken,
        String newRefreshToken
) {
}
