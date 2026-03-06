package study.blog.postlike.domain.exception;

import study.blog.global.common.exception.DomainException;

public class LikeNotFoundException extends DomainException {
    public LikeNotFoundException(String message) {
        super(message);
    }
}
