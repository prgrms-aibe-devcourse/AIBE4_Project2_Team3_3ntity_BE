package kr.java.java.domain.notification.service;

import kr.java.java.domain.notification.dto.NotificationResponse;
import kr.java.java.domain.notification.entity.Notification;
import kr.java.java.domain.notification.enums.NotificationType;
import kr.java.java.domain.notification.exception.NotificationErrorCode;
import kr.java.java.domain.notification.exception.NotificationException;
import kr.java.java.domain.notification.repository.EmitterRepository;
import kr.java.java.domain.notification.repository.NotificationRepository;
import kr.java.java.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmitterRepository emitterRepository;

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 60분

    public SseEmitter subscribe(Long userId, String lastEventId) {
        String emitterId = userId + "_" + System.currentTimeMillis();
        SseEmitter emitter = emitterRepository.saveEmitter(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> {
            log.info("[알림] SSE onCompletion callback");
            emitterRepository.deleteEmitterById(emitterId);
        });
        emitter.onTimeout(() -> {
            log.info("[알림] SSE onTimeout callback");
            emitterRepository.deleteEmitterById(emitterId);
        });
        emitter.onError((e) -> {
            log.info("[알림] SSE onError callback");
            emitterRepository.deleteEmitterById(emitterId);
        });

        sendEventToClient(emitter, emitterId, "connect", "SSE 연결되었습니다. [userId: " + userId + "]");


        if (!lastEventId.isEmpty()) {
            Map<String, Object> events = emitterRepository.findAllEventCacheStartWithUserId(String.valueOf(userId));
            events.entrySet().stream()
                    .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                    .forEach(entry -> sendEventToClient(emitter, entry.getKey(), "notification", entry.getValue()));
        }

        log.info("[알림] SSE 연결 완료, userId:" + userId);
        return emitter;
    }

    @Transactional
    public void sendNotification(Long receiverId, NotificationType notificationType, String content, String relatedUrl) {

        log.info("[알림] 알림 전송 - receiverId: {}, NotificationType: {}, content: {}, relatedUrl: {}", receiverId, notificationType, content, relatedUrl);

        Notification notification = notificationRepository.save(Notification.builder()
                .receiverId(receiverId)
                .content(content)
                .relatedUrl(relatedUrl)
                .notificationType(notificationType)
                .build());

        String receiverIdString = String.valueOf(receiverId);
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithUserId(receiverIdString);

        emitters.forEach(
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notification);

                    sendEventToClient(emitter,key,"notification",NotificationResponse.from(notification));
                }
        );
    }

    private void sendEventToClient(SseEmitter emitter, String emitterId, String eventName, Object sendData) {
        try {
            emitter.send(SseEmitter.event()
                    .id(emitterId)
                    .name(eventName)
                    .data(sendData));
            log.info("[알림] 이벤트 전송, eventName:" + eventName);
        } catch (IOException exception) {
            emitterRepository.deleteEmitterById(emitterId);
            log.error("[알림] SSE 연결 오류", exception);
        }
    }

    public List<NotificationResponse> getNotifications(Long userId, Long lastId, Boolean lastIsRead, int limit) {
        boolean isRead = (lastIsRead != null) ? lastIsRead : false;

        // 지난 알림(읽은 알림) 조회 중일 때
        if (isRead) {
            log.info("[알림] 지난 알림 추가 조회 - userId:{}, lastId: {}", userId, lastId);
            return notificationRepository.findReadNotifications(userId, lastId, PageRequest.of(0, limit))
                    .stream().map(NotificationResponse::from).collect(Collectors.toList());
        }

        // 미확인 알림 조회 중일 때
        List<Notification> unreadNotificationList = notificationRepository.findUnreadNotifications(
                userId, lastId, PageRequest.of(0, limit)
        );
        List<Notification> result = new ArrayList<>(unreadNotificationList);

        if (result.size() >= limit) {
            log.info("[알림] 미확인 알림 조회 - userId:{}, lastId: {}", userId, lastId);
            return result.stream().map(NotificationResponse::from).collect(Collectors.toList());
        }

        // 25개 미만일 시 지난 알림 추가 조회
        int remainingLimit = limit - result.size();
        List<Notification> readNotificationList = notificationRepository.findReadNotifications(userId, null, PageRequest.of(0, remainingLimit));

        result.addAll(readNotificationList);

        log.info("[알림] 미확인+확인 알림 조회 - userId:{}, lastId: {}", userId, lastId);
        return result.stream().map(NotificationResponse::from).collect(Collectors.toList());
    }

    public long getUnreadNotificationCount(Long userId) {
        log.info("[알림] 미확인 알림 개수 조회 - userId:{}", userId);
        return notificationRepository.countUnreadNotificationsByUserId(userId);
    }

    @Transactional
    public void readNotification(Long notificationId) {
        log.info("[알림] 알림 읽음 처리");
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(notificationId, NotificationErrorCode.NOTIFICATION_NOT_FOUND));
        notification.read();
    }
}
