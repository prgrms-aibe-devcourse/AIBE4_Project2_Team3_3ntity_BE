package kr.java.java.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.notification.dto.NotificationResponse;
import kr.java.java.domain.notification.exception.NotificationException;
import kr.java.java.domain.notification.exception.UserNotFoundException;
import kr.java.java.domain.notification.service.NotificationService;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/piece/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Operation(summary = "알림 구독", description = "알림 구독을 위해 SSE 연결을 실행합니다.")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {

        String userUuidString = userDetails.getUuid().toString();

        log.info("[알림 controller] SSE 구독 요청, 유저 UUID: {}", userUuidString);

         if (userRepository.findByUuid(userDetails.getUuid()).isEmpty()) {
             throw new UserNotFoundException(userDetails.getUuid());
         }

        return notificationService.subscribe(userUuidString, lastEventId);
    }

    @Operation(summary = "알림 내역 조회", description = "특정 사용자의 모든 알림 내역을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false) Boolean lastIsRead
    ) {
        UUID userUuid = userDetails.getUuid();

        if (userRepository.findByUuid(userDetails.getUuid()).isEmpty()) {
            throw new UserNotFoundException(userDetails.getUuid());
        }

        List<NotificationResponse> notifications = notificationService.getNotifications(userUuid, lastId, lastIsRead, 25);
        log.info("[알림 controller] 알림 내역 조회 성공 - userId: {}, lastId: {}, lastIsRead: {}, 조회된 개수: {}", userUuid, lastId, lastIsRead,notifications.size());
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<String> readNotification(@PathVariable Long notificationId) {
        try {
            notificationService.readNotification(notificationId);
            log.info("[알림 controller]: 알림 읽음 처리 성공 - {}", notificationId);
            return ResponseEntity.ok("Notification read");
        } catch (NotificationException e) {
            log.error("[알림 controller]: 해당 알림 찾을 수 없음 - {}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "미확인 알림 개수 확인", description = "특정 사용자의 미확인 알림 개수를 조회합니다.")
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadNotificationCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userUuid = userDetails.getUuid();

        if (userRepository.findByUuid(userDetails.getUuid()).isEmpty()) {
            throw new UserNotFoundException(userDetails.getUuid());
        }

        Long notificationCount = notificationService.getUnreadNotificationCount(userUuid);
        log.info("[알림 controller] 미확인 알림 개수 조회 성공 : 유저 ID: {}, 미확인 알림 총 {}개 ", userUuid,notificationCount);
        return ResponseEntity.ok(notificationCount);
    }
}
