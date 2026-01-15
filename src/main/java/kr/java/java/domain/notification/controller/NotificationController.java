package kr.java.java.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.java.java.domain.auth.exception.AuthErrorCode;
import kr.java.java.domain.auth.exception.AuthException;
import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.notification.dto.NotificationResponse;
import kr.java.java.domain.notification.enums.NotificationType;
import kr.java.java.domain.notification.exception.NotificationNotFoundException;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SSE 연결 성공", content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "마지막 이벤트 ID (재연결 시 사용)", required = false) @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {

        if (userDetails == null) {
            log.error("[알림 controller] 인증되지 않은 사용자 접근");
            throw new AuthException(AuthErrorCode.OAUTH2_USER_NOT_FOUND);
        }

        String userUuidString = userDetails.getUuid().toString();

        log.info("[알림 controller] SSE 구독 요청, 유저 UUID: {}", userUuidString);

         if (userRepository.findByUuid(userDetails.getUuid()).isEmpty()) {
             throw new AuthException(AuthErrorCode.OAUTH2_USER_NOT_FOUND);
         }

        return notificationService.subscribe(userUuidString, lastEventId);
    }

    @Operation(summary = "알림 내역 조회", description = "특정 사용자의 모든 알림 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 내역 조회 성공", content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "마지막 알림 ID (페이징용)", required = false) @RequestParam(required = false) Long lastId,
            @Parameter(description = "마지막 알림 읽음 여부 (페이징용)", required = false) @RequestParam(required = false) Boolean lastIsRead
    ) {
        if (userDetails == null) {
            log.error("[알림 controller] 인증되지 않은 사용자 접근");
            throw new AuthException(AuthErrorCode.OAUTH2_USER_NOT_FOUND);
        }

        UUID userUuid = userDetails.getUuid();

        if (userRepository.findByUuid(userDetails.getUuid()).isEmpty()) {
            throw new AuthException(AuthErrorCode.OAUTH2_USER_NOT_FOUND);
        }

        List<NotificationResponse> notifications = notificationService.getNotifications(userUuid, lastId, lastIsRead, 25);
        log.info("[알림 controller] 알림 내역 조회 성공 - userId: {}, lastId: {}, lastIsRead: {}, 조회된 개수: {}", userUuid, lastId, lastIsRead,notifications.size());
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공"),
            @ApiResponse(responseCode = "404", description = "해당 알림을 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<String> readNotification(
            @Parameter(description = "알림 ID", required = true) @PathVariable Long notificationId) {
        try {
            notificationService.readNotification(notificationId);
            log.info("[알림 controller]: 알림 읽음 처리 성공 - {}", notificationId);
            return ResponseEntity.ok("Notification read");
        } catch (NotificationNotFoundException e) {
            log.error("[알림 controller]: 해당 알림 찾을 수 없음 - {}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "미확인 알림 개수 확인", description = "특정 사용자의 미확인 알림 개수를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "미확인 알림 개수 조회 성공", content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadNotificationCount(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            log.error("[알림 controller] 인증되지 않은 사용자 접근");
            throw new AuthException(AuthErrorCode.OAUTH2_USER_NOT_FOUND);
        }

        UUID userUuid = userDetails.getUuid();

        if (userRepository.findByUuid(userDetails.getUuid()).isEmpty()) {
            throw new AuthException(AuthErrorCode.OAUTH2_USER_NOT_FOUND);
        }

        Long notificationCount = notificationService.getUnreadNotificationCount(userUuid);
        log.info("[알림 controller] 미확인 알림 개수 조회 성공 : 유저 ID: {}, 미확인 알림 총 {}개 ", userUuid,notificationCount);
        return ResponseEntity.ok(notificationCount);
    }

    @Operation(summary = "테스트용 알림 생성", description = "테스트용 알림 25개를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "테스트 알림 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/test-data")
    public ResponseEntity<String> createTestNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            log.error("[알림 controller] 인증되지 않은 사용자 접근");
            throw new AuthException(AuthErrorCode.OAUTH2_USER_NOT_FOUND);
        }

        UUID userUuid = userDetails.getUuid();

        for (int i = 1; i <= 25; i++) {
            notificationService.createNotification(
                    userUuid,
                    NotificationType.MATCHING,
                    "USER " + i + "님이 매칭을 신청했습니다.",
                    "/matchings"
            );
        }

        return ResponseEntity.ok("테스트 알림 25개 생성 완료");
    }
}
