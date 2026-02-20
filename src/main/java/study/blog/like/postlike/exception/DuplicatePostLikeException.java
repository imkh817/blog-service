package study.blog.like.postlike.exception;

import study.blog.global.common.exception.DomainException;

public class DuplicatePostLikeException extends DomainException {
    public DuplicatePostLikeException(String message) {
        super(message);
    }
}
