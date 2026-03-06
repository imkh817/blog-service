package study.blog.comment.domain.exception;

import study.blog.global.common.exception.DomainException;

public class TooManyContentLengthException extends DomainException {
    public TooManyContentLengthException(String message) {
        super(message);
    }
}
