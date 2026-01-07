package kr.java.java.domain.notification.exception;

import lombok.Getter;

@Getter
public class NotificationException extends RuntimeException {
    private final NotificationErrorCode errorCode;

    public NotificationException(Long errorInfo, NotificationErrorCode notificationErrorCode) {
        super(notificationErrorCode.getMessage()+errorInfo);
        this.errorCode = notificationErrorCode;
    }
}
