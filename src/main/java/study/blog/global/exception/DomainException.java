package study.blog.global.exception;

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
