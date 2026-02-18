package study.blog.member.dto;

import study.blog.member.entity.Member;

public record MemberResponse(
        Long id,
        String email,
        String nickname
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname()
        );
    }
}
