package study.blog.subscription.domain.exception;

import study.blog.global.common.exception.DomainException;

public class DuplicateSubscriptionException extends DomainException {
    public DuplicateSubscriptionException(String message) {
        super(message);
    }
}
