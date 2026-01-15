package kr.java.java.domain.image.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ImageErrorCode {
    DB_IMAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "DB에 해당 이미지가 존재하지 않습니다."),
    S3_IMAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "외부 저장소에 해당 이미지가 존재하지 않습니다."),
    SPACE_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 공간의 이미지가 존재하지 않습니다."),
    UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패하였습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다. 이미지 파일만 업로드 가능합니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ImageErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
