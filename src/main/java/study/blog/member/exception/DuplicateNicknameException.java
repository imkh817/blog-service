package study.blog.member.exception;

import study.blog.global.common.exception.DomainException;

public class DuplicateNicknameException extends DomainException {
    public DuplicateNicknameException(String message) {
        super(message);
    }
}
