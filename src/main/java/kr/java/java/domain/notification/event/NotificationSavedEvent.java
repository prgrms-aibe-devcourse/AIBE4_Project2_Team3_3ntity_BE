package kr.java.java.domain.notification.event;

public record NotificationSavedEvent(
        String receiverId,
        Long notificationId
){}
