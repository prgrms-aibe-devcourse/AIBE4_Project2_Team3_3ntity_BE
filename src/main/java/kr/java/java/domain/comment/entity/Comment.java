package kr.java.java.domain.comment.entity;

import jakarta.persistence.*;
import kr.java.java.domain.portfolio.entity.Portfolio;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "Comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comments_id")
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_secret", nullable = false)
    private boolean isSecret;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 공간
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spaces_id", nullable = true)
    private Space space;

    // 포트폴리오
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = true)
    private Portfolio portfolio;

    @Builder
    public Comment(String content, boolean isSecret, User user, Space space, Portfolio portfolio) {
        this.content = content;
        this.isSecret = isSecret;
        this.user = user;
        this.space = space;
        this.portfolio = portfolio;
    }

    // 수정 메서드 (내용, 비밀글 여부 변경)
    public void update(String content, Boolean isSecret) {
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
        if (isSecret != null) {
            this.isSecret = isSecret;
        }
    }
}
