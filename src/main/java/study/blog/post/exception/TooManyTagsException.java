package study.blog.post.exception;

import study.blog.global.exception.DomainException;

public class TooManyTagsException extends DomainException {
    public TooManyTagsException(String message) {
        super(message);
    }
}
