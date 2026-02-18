package study.blog.auth.dto;

import study.blog.member.dto.MemberResponse;

public record LoginResponse(
        String accessToken,
        MemberResponse user
) {
}
