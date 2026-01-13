package kr.java.java.domain.review.entity;

import jakarta.persistence.*;
import kr.java.java.domain.matching.entity.Matching;
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
@Table(name = "Reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reviews_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id")
    private Matching matching;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Review(Matching matching, Integer rating, String content, User user) {
        this.matching = matching;
        this.rating = rating;
        this.content = content;
        this.user = user;
    }

    public void updateReview(Integer rating, String content) {
        if (rating != null) {
            this.rating = rating;
        }

        if (content != null && !content.isBlank()) {
            this.content = content;
        }
    }
}
