package study.blog.post.exception;

import study.blog.global.common.exception.DomainException;

public class InValidPostTitleException extends DomainException {
    public InValidPostTitleException(String message) {
        super(message);
    }
}
