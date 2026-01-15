package kr.java.java.domain.notification.exception;

public class NotificationSendFailedException extends RuntimeException {
    public NotificationSendFailedException(String message) {
        super(message);
    }
}
