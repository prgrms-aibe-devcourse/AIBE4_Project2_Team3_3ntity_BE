package kr.java.java.domain.matching.exception;

import lombok.Getter;

@Getter
public class MatchingException extends RuntimeException {
    private final MatchingErrorCode errorCode;

    public MatchingException(MatchingErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}