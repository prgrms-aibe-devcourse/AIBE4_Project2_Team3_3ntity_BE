package kr.java.java.domain.user.entity;

import jakarta.persistence.*;
import kr.java.java.domain.comment.entity.Comment;
import kr.java.java.domain.favorite.entity.Favorite;
import kr.java.java.domain.portfolio.entity.Portfolio;
import kr.java.java.domain.review.entity.Review;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.global.util.ProfileImageUrlGenerator;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="user_id", unique = true, nullable = false, columnDefinition = "BINARY(16)")
    private UUID uuid;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "profile_image_url", columnDefinition = "TEXT")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 20)
    private Provider provider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Favorite> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Portfolio> portfolios = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Space> spaces = new ArrayList<>();

    @PrePersist
    public void createUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    public void upgradeToHost() {
        if (this.role == Role.USER) {
            this.role = Role.HOST;
        }
    }

    public boolean isHost() {
        return this.role == Role.HOST;
    }

    public boolean isUser() {
        return this.role == Role.USER;
    }

    public boolean isCustomImage(String imageUrl) {
        return this.profileImageUrl != null && !this.profileImageUrl.contains("api.dicebear.com");
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.nickname = "탈퇴한 회원"; // 닉네임을 덮어씌움
        this.profileImageUrl = ProfileImageUrlGenerator.getDeletedUserImage(); // 회색 바탕 이미지로 변경
    }
}