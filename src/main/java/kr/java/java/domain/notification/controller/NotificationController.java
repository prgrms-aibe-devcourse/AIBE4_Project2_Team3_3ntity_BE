package kr.java.java.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.java.java.domain.notification.dto.NotificationResponse;
import kr.java.java.domain.notification.exception.NotificationNotFoundException;
import kr.java.java.domain.notification.service.NotificationService;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/piece/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    //to do: SSE 연결

    @Operation(summary = "알림 내역 조회", description = "특정 사용자의 모든 알림 내역을 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotifications(@PathVariable Long userId) {
        log.info("[알림 api] 알림 내역 조회, 유저 ID: {}", userId);
//        if (!userRepository.existsById(userId)) {
//            throw new UserNotFoundException(userId);
//        }
        List<NotificationResponse> notifications = notificationService.getNotifications(userId);
        log.info("[알림 api] {} 조회 성공, 유저 ID: {}", notifications.size(), userId);
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
        } catch (NotificationNotFoundException e) {
            log.error("[알림 api]: 해당 알림 찾을 수 없음 - {}", e.getMessage());
            throw e;
        }
    }

//    //테스트 알림 생성
//    @PostMapping("/test/{userId}")
//    public ResponseEntity<String> createTestNotification(@PathVariable Long userId,
//                                                         @RequestParam String content,
//                                                         @RequestParam String relatedUrl,
//                                                         @RequestParam NotificationType type) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new UserNotFoundException(userId));
//
//        notificationService.createNotification(user, content, relatedUrl, type);
//        return ResponseEntity.ok("Notification created");
//    }
}
