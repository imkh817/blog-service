package study.blog.member.exception;

import study.blog.global.common.exception.DomainException;

public class InvalidMemberException extends DomainException {
    public InvalidMemberException(String message) {
        super(message);
    }
}
