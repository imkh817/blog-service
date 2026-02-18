package study.blog.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import study.blog.global.common.entity.BaseEntity;
import study.blog.member.enums.MemberRole;
import study.blog.member.exception.InvalidMemberException;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;
import static org.springframework.util.StringUtils.hasText;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Member extends BaseEntity {

    private static final int MAX_NICKNAME_LENGTH = 20;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    public static Member createMember(String email, String encodedPassword, String nickname) {
        validateEmail(email);
        validatePassword(encodedPassword);
        validateNickname(nickname);

        Member member = new Member();
        member.email = email;
        member.password = encodedPassword;
        member.nickname = nickname;
        member.role = MemberRole.ROLE_USER;
        return member;
    }

    private static void validateEmail(String email) {
        if (!hasText(email)) {
            throw new InvalidMemberException("이메일은 필수입니다.");
        }
        if (!email.matches(EMAIL_REGEX)) {
            throw new InvalidMemberException("올바른 이메일 형식이 아닙니다.");
        }
    }

    private static void validatePassword(String password) {
        if (!hasText(password)) {
            throw new InvalidMemberException("비밀번호는 필수입니다.");
        }
    }

    private static void validateNickname(String nickname) {
        if (!hasText(nickname)) {
            throw new InvalidMemberException("닉네임은 필수입니다.");
        }
        if (nickname.length() > MAX_NICKNAME_LENGTH) {
            throw new InvalidMemberException("닉네임은 최대 " + MAX_NICKNAME_LENGTH + "자까지 가능합니다.");
        }
    }
}
