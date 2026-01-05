package kr.java.java.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.java.java.domain.notification.dto.NotificationResponse;
import kr.java.java.domain.notification.enums.NotificationType;
import kr.java.java.domain.notification.service.NotificationService;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<String> readNotification(@PathVariable Long notificationId) {
        notificationService.readNotification(notificationId);
        return ResponseEntity.ok("Read notification");
    }

//    //테스트용 알림 생성
//    @PostMapping("/test/{userId}")
//    public ResponseEntity<String> createTestNotification(@PathVariable Long userId,
//                                                         @RequestParam String content,
//                                                         @RequestParam String relatedUrl,
//                                                         @RequestParam NotificationType type) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//
//        notificationService.createNotification(user, content, relatedUrl, type);
//        return ResponseEntity.ok("Notification created");
//    }
}
