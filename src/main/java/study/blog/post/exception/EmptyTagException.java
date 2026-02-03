package study.blog.post.exception;

import study.blog.global.exception.DomainException;

public class EmptyTagException extends DomainException {
    public EmptyTagException(String message) {
        super(message);
    }
}
