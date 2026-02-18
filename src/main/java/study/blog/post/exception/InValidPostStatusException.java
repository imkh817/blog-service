package study.blog.post.exception;

import study.blog.global.common.exception.DomainException;

public class InValidPostStatusException extends DomainException {
    public InValidPostStatusException(String message) {
        super(message);
    }
}
