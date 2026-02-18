package study.blog.auth.exception;

import study.blog.global.common.exception.DomainException;

public class TokenTamperedException extends DomainException {

    public TokenTamperedException(String message) {
        super(message);
    }
}
