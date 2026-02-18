package study.blog.post.exception;

import study.blog.global.common.exception.DomainException;

public class InValidPostContentException extends DomainException {
    public InValidPostContentException(String message) {
        super(message);
    }
}
