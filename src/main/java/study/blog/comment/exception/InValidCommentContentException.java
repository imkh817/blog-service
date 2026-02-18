package study.blog.comment.exception;

import study.blog.global.common.exception.DomainException;

public class InValidCommentContentException extends DomainException {
    public InValidCommentContentException(String message) {
        super(message);
    }
}
