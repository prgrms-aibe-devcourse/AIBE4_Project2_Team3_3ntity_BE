package kr.java.java.domain.notification.service;

import kr.java.java.domain.notification.dto.NotificationResponse;
import kr.java.java.domain.notification.entity.Notification;
import kr.java.java.domain.notification.enums.NotificationType;
import kr.java.java.domain.notification.event.NotificationSavedEvent;
import kr.java.java.domain.notification.exception.NotificationErrorCode;
import kr.java.java.domain.notification.exception.NotificationException;
import kr.java.java.domain.notification.repository.EmitterRepository;
import kr.java.java.domain.notification.repository.NotificationRepository;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmitterRepository emitterRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 60분

    public SseEmitter subscribe(String userUuidString, String lastEventId) {
        String emitterId = userUuidString + "_" + System.currentTimeMillis();
        SseEmitter emitter = emitterRepository.saveEmitter(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> {
            log.info("[알림 service] SSE onCompletion callback");
            emitterRepository.deleteEmitterById(emitterId);
        });
        emitter.onTimeout(() -> {
            log.info("[알림 service] SSE onTimeout callback");
            emitterRepository.deleteEmitterById(emitterId);
        });
        emitter.onError((e) -> {
            log.info("[알림 service] SSE onError callback");
            emitterRepository.deleteEmitterById(emitterId);
        });

        sendEventToClient(emitter, emitterId, "connect", "SSE 연결되었습니다. [userUuid: " + userUuidString + "]");


        if (!lastEventId.isEmpty()) {
            Map<String, Object> events = emitterRepository.findAllEventCacheStartWithUserId(userUuidString);
            events.entrySet().stream()
                    .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                    .forEach(entry -> sendEventToClient(emitter, entry.getKey(), "notification", entry.getValue()));
        }

        log.info("[알림 service] SSE 연결 완료, userUuid:" + userUuidString);
        return emitter;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNotification(UUID receiverId, NotificationType notificationType, String content, String relatedUrl) {

        log.info("[알림 service] 알림 생성 - receiverId: {}, NotificationType: {}, content: {}, relatedUrl: {}", receiverId, notificationType, content, relatedUrl);

        Notification notification = notificationRepository.save(
                Notification.builder()
                .receiverId(receiverId)
                .content(content)
                .relatedUrl(relatedUrl)
                .notificationType(notificationType)
                .build()
        );
        
        String receiverIdString = receiverId.toString();

        applicationEventPublisher.publishEvent(
                new NotificationSavedEvent(receiverIdString, notification.getId())
        );
    }

    public void sendEventToClient(SseEmitter emitter, String emitterId, String eventName, Object sendData) {
        try {
            emitter.send(SseEmitter.event()
                    .id(emitterId)
                    .name(eventName)
                    .data(sendData));
            log.info("[알림 service] sendToClient - 이벤트 전송, eventName:" + eventName);
        } catch (IOException exception) {
            emitterRepository.deleteEmitterById(emitterId);
            log.error("[알림 service] sendToClient - SSE 연결 오류", exception);
        }
    }

    public List<NotificationResponse> getNotifications(UUID userId, Long lastId, Boolean lastIsRead, int limit) {
        boolean isRead = (lastIsRead != null) ? lastIsRead : false;

        // 지난 알림(읽은 알림) 조회 중일 때
        if (isRead) {
            log.info("[알림 service] 지난 알림 추가 조회 - userId:{}, lastId: {}", userId, lastId);
            return notificationRepository.findReadNotifications(userId, lastId, PageRequest.of(0, limit))
                    .stream().map(NotificationResponse::from).collect(Collectors.toList());
        }

        // 미확인 알림 조회 중일 때
        List<Notification> unreadNotificationList = notificationRepository.findUnreadNotifications(
                userId, lastId, PageRequest.of(0, limit)
        );
        List<Notification> result = new ArrayList<>(unreadNotificationList);

        if (result.size() >= limit) {
            log.info("[알림 service] 미확인 알림 조회 - userId:{}, lastId: {}", userId, lastId);
            return result.stream().map(NotificationResponse::from).collect(Collectors.toList());
        }

        // 25개 미만일 시 지난 알림 추가 조회
        int remainingLimit = limit - result.size();
        List<Notification> readNotificationList = notificationRepository.findReadNotifications(userId, null, PageRequest.of(0, remainingLimit));

        result.addAll(readNotificationList);

        log.info("[알림 service] 미확인+확인 알림 조회 - userId:{}, lastId: {}", userId, lastId);
        return result.stream().map(NotificationResponse::from).collect(Collectors.toList());
    }

    public long getUnreadNotificationCount(UUID userId) {
        log.info("[알림 service] 미확인 알림 개수 조회 - userId:{}", userId);
        return notificationRepository.countUnreadNotificationsByUserId(userId);
    }

    @Transactional
    public void readNotification(Long notificationId) {
        log.info("[알림 service] 알림 읽음 처리");
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new NotificationException(notificationId, NotificationErrorCode.NOTIFICATION_NOT_FOUND));
        notification.read();
    }
}
