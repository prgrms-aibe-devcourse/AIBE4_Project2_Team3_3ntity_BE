package kr.java.java.domain.image.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ImageErrorCode {
    DB_IMAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "DB에 해당 이미지가 존재하지 않습니다."),
    S3_IMAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "외부 저장소에 해당 이미지가 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ImageErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}