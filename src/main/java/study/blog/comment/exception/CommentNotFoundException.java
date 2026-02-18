package study.blog.comment.exception;

import study.blog.global.common.exception.DomainException;

public class CommentNotFoundException extends DomainException {
    public CommentNotFoundException(String message) {
        super(message);
    }
}
