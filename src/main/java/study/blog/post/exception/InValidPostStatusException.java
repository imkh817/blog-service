package study.blog.post.exception;

import study.blog.global.exception.DomainException;

public class InValidPostStatusException extends DomainException {
    public InValidPostStatusException(String message) {
        super(message);
    }
}
