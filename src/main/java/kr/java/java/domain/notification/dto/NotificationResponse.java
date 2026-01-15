package kr.java.java.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.java.java.domain.notification.entity.Notification;
import kr.java.java.domain.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 응답 DTO")
public class NotificationResponse {
    @Schema(description = "알림 ID", example = "1")
    private Long id;

    @Schema(description = "알림 내용", example = "(상대방 닉네임)님이 매칭을 신청했습니다.")
    private String content;

    @Schema(description = "관련 URL", example = "/matchings/1")
    private String relatedUrl;

    @Schema(description = "읽음 여부", example = "false")
    private boolean isRead;

    @Schema(description = "알림 타입", example = "MATCHING")
    private NotificationType notificationType;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .content(notification.getContent())
                .relatedUrl(notification.getRelatedUrl())
                .isRead(notification.isRead())
                .notificationType(notification.getNotificationType())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
