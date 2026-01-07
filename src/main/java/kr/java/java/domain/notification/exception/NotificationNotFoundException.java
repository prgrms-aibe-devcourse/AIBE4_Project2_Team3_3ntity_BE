package kr.java.java.domain.notification.exception;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(Long notificationId) {
        super("[Error] 알림을 찾을 수 없습니다. 알림 ID: " + notificationId);
    }
}
