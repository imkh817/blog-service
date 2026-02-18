package study.blog.comment.exception;

import study.blog.global.common.exception.DomainException;

public class TooManyContentLengthException extends DomainException {
    public TooManyContentLengthException(String message) {
        super(message);
    }
}
