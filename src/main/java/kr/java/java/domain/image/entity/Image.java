package kr.java.java.domain.image.entity;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import kr.java.java.domain.portfolio.entity.Portfolio;
import kr.java.java.domain.review.entity.Review;
import kr.java.java.domain.space.entity.Space;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private Space space;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Image(String targetType, String fileUrl, Integer sortOrder,
                 Review review, Space space, Portfolio portfolio) {
        this.targetType = targetType;
        this.fileUrl = fileUrl;
        this.sortOrder = sortOrder;
        this.review = review;
        this.space = space;
        this.portfolio = portfolio;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}