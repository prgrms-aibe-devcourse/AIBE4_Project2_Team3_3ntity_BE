package kr.java.java.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.java.java.domain.notification.dto.NotificationResponse;
import kr.java.java.domain.notification.enums.NotificationType;
import kr.java.java.domain.notification.exception.NotificationException;
import kr.java.java.domain.notification.exception.UserNotFoundException;
import kr.java.java.domain.notification.service.NotificationService;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/piece/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Operation(summary = "알림 구독", description = "알림 구독을 위해 SSE 연결을 실행합니다.")
    @GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long userId,
                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        log.info("[알림 api] SSE 구독 요청, 유저 ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return notificationService.subscribe(userId, lastEventId);
    }

    @Operation(summary = "알림 내역 조회", description = "특정 사용자의 모든 알림 내역을 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @PathVariable Long userId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false) Boolean lastIsRead
    ) {
        log.info("[알림 api] 알림 내역 조회 요청 - userId: {}, lastId: {}, lastIsRead: {}", userId, lastId, lastIsRead);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        List<NotificationResponse> notifications = notificationService.getNotifications(userId, lastId, lastIsRead, 25);
        log.info("[알림 api] 조회 성공 - userId: {}, 조회된 개수: {}", userId, notifications.size());
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<String> readNotification(@PathVariable Long notificationId) {
        log.info("[알림 api]: 알림 읽음 처리 - {}", notificationId);
        try {
            notificationService.readNotification(notificationId);
            log.info("[알림 api]: 알림 읽음 처리 성공 - {}", notificationId);
            return ResponseEntity.ok("Notification read");
        } catch (NotificationException e) {
            log.error("[알림 api]: 해당 알림 찾을 수 없음 - {}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "미확인 알림 개수 확인", description = "특정 사용자의 미확인 알림 개수를 조회합니다.")
    @GetMapping("/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadNotificationCount(@PathVariable Long userId) {
        log.info("[알림 api] 미확인 알림 개수 조회, 유저 ID: {} ", userId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        Long notificationCount = notificationService.getUnreadNotificationCount(userId);
        log.info("[알림 api] 조회 성공 : 총 {}개 ", notificationCount);
        return ResponseEntity.ok(notificationCount);
    }

//    @PostMapping("/test-send/{userId}")
//        public String testSend(@PathVariable Long userId) {
//            User user = userRepository.findById(userId)
//                    .orElseThrow(() -> new UserNotFoundException(userId));
//            notificationService.sendNotification(user, NotificationType.MATCHING_COMPLETE,"테스트 : 매칭이 완료되었습니다.","piece/matchings/1");
//        return "알림 발송 성공 (User ID: " + userId + ")";
//    }
}
