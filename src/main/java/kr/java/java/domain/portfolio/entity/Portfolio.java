package kr.java.java.domain.portfolio.entity;

import jakarta.persistence.*;
import kr.java.java.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long id;

    @Column(name = "brand_name", length = 100)
    private String brandName;

    @Column(name = "title",nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "external_link", columnDefinition = "TEXT")
    private String externalLink;

    @Column(name = "is_open", nullable = false)
    private boolean isOpen;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Builder
    public Portfolio(String brandName, String title, String description, String category, String externalLink, boolean isOpen, LocalDateTime createdAt,User user) {
        this.brandName = brandName;
        this.title = title;
        this.description = description;
        this.category = category;
        this.externalLink = externalLink;
        this.isOpen = isOpen;
        this.createdAt = LocalDateTime.now();
        this.user = user;
    }
}
