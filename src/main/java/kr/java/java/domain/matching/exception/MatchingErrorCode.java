package kr.java.java.domain.matching.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MatchingErrorCode {
    SELF_MATCHING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "본인과의 매칭은 진행할 수 없습니다."),
    ALREADY_ACTIVE_MATCHING_EXISTS(HttpStatus.CONFLICT, "이미 대기 중이거나 진행 중인 매칭이 존재합니다."),
    MATCHING_NOT_FOUND(HttpStatus.NOT_FOUND, "매칭을 찾을 수 없습니다."),
    NOT_AUTHORIZED_RECEIVER(HttpStatus.FORBIDDEN, "매칭 수신자가 아닙니다."),
    NOT_AUTHORIZED_SENDER(HttpStatus.FORBIDDEN, "매칭 발신자가 아닙니다."),
    INVALID_MATCH_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 매칭 상태입니다."),
    ALREADY_FINALIZED_MATCHING(HttpStatus.BAD_REQUEST, "이미 최종 처리된 매칭입니다."),
    MATCHING_REQUEST_ALREADY_RECEIVED(HttpStatus.CONFLICT, "상대방의 신청을 확인해주세요"),
    HOST_CANNOT_MATCH_OTHER_SPACE(HttpStatus.FORBIDDEN, "해당 공간의 주인이 아닙니다."),
    INVALID_START_DATE(HttpStatus.BAD_REQUEST, "매칭 시작일은 필수 입력 항목입니다."),
    START_DATE_CANNOT_BE_PAST(HttpStatus.BAD_REQUEST, "매칭 시작일은 오늘 이후여야 합니다."),
    INVALID_END_DATE(HttpStatus.BAD_REQUEST, "매칭 종료일은 시작일보다 빠를 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    MatchingErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
