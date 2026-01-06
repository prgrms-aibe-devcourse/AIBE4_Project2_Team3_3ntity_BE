package kr.java.java.domain.matching.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MatchingErrorCode {
    SELF_MATCHING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "본인과의 매칭은 진행할 수 없습니다."),
    ALREADY_ACTIVE_MATCHING_EXISTS(HttpStatus.CONFLICT, "이미 대기 중이거나 진행 중인 매칭이 존재합니다.");

    private final HttpStatus httpStatus;
    private final String message;

    MatchingErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
