package study.blog.subscription.domain.exception;

import study.blog.global.common.exception.DomainException;

public class SubscriptionNotFoundException extends DomainException {
    public SubscriptionNotFoundException(String message) {
        super(message);
    }
}
