package kr.java.java.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INVALID_USER_INPUT(HttpStatus.BAD_REQUEST, "잘못된 사용자 입력입니다."),
    CANNOT_DELETE_USER_WITH_ACTIVE_MATCHING(HttpStatus.BAD_REQUEST, "진행 중인 예약이 있어 탈퇴할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}