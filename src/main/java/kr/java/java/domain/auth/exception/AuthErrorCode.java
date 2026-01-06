package kr.java.java.domain.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode {

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰이 없습니다."),
    TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "일치하지 않는 토큰입니다."),
    TOKEN_REUSE_DETECTED(HttpStatus.UNAUTHORIZED, "토큰 재사용이 감지되었습니다. 모든 세션이 종료되었습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INVALID_USER_INFO(HttpStatus.BAD_REQUEST, "사용자 정보가 올바르지 않습니다."),
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 Provider입니다."),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "필수 필드가 누락되었습니다."),
    OAUTH2_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "소셜 로그인에 실패했습니다."),
    OAUTH2_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "소셜 로그인 사용자 정보를 찾을 수 없습니다."),
    OAUTH2_EMAIL_NOT_PROVIDED(HttpStatus.BAD_REQUEST, "이메일 정보가 제공되지 않았습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}