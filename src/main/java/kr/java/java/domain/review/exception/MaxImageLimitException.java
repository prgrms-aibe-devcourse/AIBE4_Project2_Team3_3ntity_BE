package kr.java.java.domain.review.exception;

public class MaxImageLimitException extends RuntimeException {
    public MaxImageLimitException(String message) {
        super(message);
    }
}
