package kr.java.java.domain.review.exception;

public class MatchingNotFoundException extends RuntimeException {
    public MatchingNotFoundException(String message) {
        super(message);
    }
}
