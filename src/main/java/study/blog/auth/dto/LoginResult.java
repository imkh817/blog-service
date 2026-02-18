package study.blog.auth.dto;

import study.blog.member.dto.MemberResponse;

public record LoginResult(
        String accessToken,
        String refreshToken,
        MemberResponse member
) {
}
