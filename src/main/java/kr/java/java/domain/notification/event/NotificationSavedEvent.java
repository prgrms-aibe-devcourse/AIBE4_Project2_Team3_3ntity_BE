package kr.java.java.domain.notification.event;

import kr.java.java.domain.notification.entity.Notification;

public record NotificationSavedEvent(
        String receiverId,
        Notification notification
){}
