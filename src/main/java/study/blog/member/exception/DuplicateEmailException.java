package study.blog.member.exception;

import study.blog.global.common.exception.DomainException;

public class DuplicateEmailException extends DomainException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
