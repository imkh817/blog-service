package study.blog.post.exception;

import study.blog.global.exception.DomainException;

public class PostNotFoundException extends DomainException {
    public PostNotFoundException(String message) {
        super(message);
    }
}
