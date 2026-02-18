package study.blog.member.exception;

import study.blog.global.common.exception.DomainException;

public class MemberNotFoundException extends DomainException {
    public MemberNotFoundException(String message) {
        super(message);
    }
}
