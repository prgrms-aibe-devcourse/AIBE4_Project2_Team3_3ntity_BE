package kr.java.java.domain.notification.service;

import kr.java.java.domain.notification.dto.NotificationResponse;
import kr.java.java.domain.notification.entity.Notification;
import kr.java.java.domain.notification.enums.NotificationType;
import kr.java.java.domain.notification.exception.NotificationNotFoundException;
import kr.java.java.domain.notification.repository.NotificationRepository;
import kr.java.java.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification createNotification(User receiver, String content, String relatedUrl, NotificationType notificationType) {
        Notification notification = Notification.builder()
                .receiver(receiver)
                .content(content)
                .relatedUrl(relatedUrl)
                .notificationType(notificationType)
                .build();
        log.info("[알림] 알림 생성");
        return notificationRepository.save(notification);
    }

    public List<NotificationResponse> getNotifications(Long userId) {
        log.info("[알림] 알림 조회");
        return notificationRepository.findAllByReceiverIdOrderByCreatedAt(userId).stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void readNotification(Long notificationId) {
        log.info("[알림] 알림 읽음 처리");
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        notification.read();
    }
}
