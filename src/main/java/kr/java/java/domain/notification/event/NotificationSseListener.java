package kr.java.java.domain.notification.event;

import kr.java.java.domain.notification.dto.NotificationResponse;
import kr.java.java.domain.notification.entity.Notification;
import kr.java.java.domain.notification.exception.NotificationErrorCode;
import kr.java.java.domain.notification.exception.NotificationException;
import kr.java.java.domain.notification.repository.EmitterRepository;
import kr.java.java.domain.notification.repository.NotificationRepository;
import kr.java.java.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSseListener {
    private final NotificationService notificationService;
    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationSavedEvent(NotificationSavedEvent event) {
        log.info("[알림 SSE 리스너] 알림 저장 이벤트 수신 - notificationId: {}, receiverId: {}", event.notificationId(), event.receiverId());

        Notification notification = notificationRepository
                .findById(event.notificationId())
                .orElseThrow(() -> new NotificationException(event.notificationId(), NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithUserId(event.receiverId());
        
        if (emitters.isEmpty()) {
            log.info("[알림 SSE 리스너] 연결된 Emitter가 없습니다. receiverId: {}", event.receiverId());
            return;
        }

        emitters.forEach(
                (key, emitter) -> {
                    log.info("[알림 SSE 리스너] 알림 전송 시도 - emitterId: {}", key);
                    emitterRepository.saveEventCache(key, notification);

                    notificationService.sendEventToClient(emitter,key,"notification", NotificationResponse.from(notification));
                }
        );
    }
}
