package study.blog.post.domain.exception;

import study.blog.global.common.exception.DomainException;

public class InValidThumbnailUrlException extends DomainException {
    public InValidThumbnailUrlException(String message) {
        super(message);
    }
}