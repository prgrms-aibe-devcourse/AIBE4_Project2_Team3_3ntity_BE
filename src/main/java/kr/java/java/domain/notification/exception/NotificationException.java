package kr.java.java.domain.notification.exception;

import lombok.Getter;

@Getter
public class NotificationException extends RuntimeException {
    private final NotificationErrorCode errorCode;
    private final Long id;

    public NotificationException(Long id, NotificationErrorCode errorCode) {
        super(errorCode.getMessage());
        this.id = id;
        this.errorCode = errorCode;
    }
}
