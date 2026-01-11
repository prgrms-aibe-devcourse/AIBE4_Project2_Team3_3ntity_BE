package kr.java.java.domain.notification.entity;

import jakarta.persistence.*;
import kr.java.java.domain.notification.enums.NotificationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    // TODO: uuid로 변경
    @Column(name = "user_id", nullable = false)
    private Long receiverId;

    @Column(nullable = false)
    private String content;

    @Column(name = "related_url")
    private String relatedUrl;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Notification(Long receiverId, String content, String relatedUrl, NotificationType notificationType) {
        this.receiverId = receiverId;
        this.content = content;
        this.relatedUrl = relatedUrl;
        this.notificationType = notificationType;
        this.isRead = false;
    }

    public void read() {
        this.isRead = true;
    }
}
