package study.blog.auth.exception;

import study.blog.global.common.exception.DomainException;

public class InvalidRefreshTokenException extends DomainException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
