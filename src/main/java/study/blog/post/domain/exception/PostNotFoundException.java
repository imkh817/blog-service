package study.blog.post.domain.exception;

import study.blog.global.common.exception.DomainException;

public class PostNotFoundException extends DomainException {
    public PostNotFoundException(String message) {
        super(message);
    }
}
