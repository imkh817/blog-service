package study.blog.like.postlike.exception;

import study.blog.global.common.exception.DomainException;

public class LikeNotFoundException extends DomainException {
    public LikeNotFoundException(String message) {
        super(message);
    }
}
