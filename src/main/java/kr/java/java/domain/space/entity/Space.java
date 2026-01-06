package kr.java.java.domain.space.entity;

import jakarta.persistence.*;
import kr.java.java.domain.matching.entity.Matching;
import kr.java.java.domain.space.dto.SpaceUpdateRequest;
import kr.java.java.domain.space.enums.SpaceStatus;
import kr.java.java.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "spaces")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "space_id")
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "detail_address", length = 255)
    private String detailAddress;

    @Column(name ="latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "price_per_month")
    private Integer pricePerMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private SpaceStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "space")
    private List<Matching> matchings = new ArrayList<>();

    @Builder
    public Space(String title, String description, String category, String address, String detailAddress, BigDecimal latitude, BigDecimal longitude, Integer pricePerMonth,User user){
        this.title = title;
        this.description = description;
        this.category = category;
        this.address = address;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pricePerMonth = pricePerMonth;
        this.user = user;
        this.status = SpaceStatus.RECRUITING;
    }

    public void update(SpaceUpdateRequest request) {
        if (request.title() != null) this.title = request.title();
        if (request.description() != null) this.description = request.description();
        if (request.category() != null) this.category = request.category();
        if (request.address() != null) this.address = request.address();
        if (request.detailAddress() != null) this.detailAddress = request.detailAddress();

        if (request.latitude() != null) {
            this.latitude = BigDecimal.valueOf(request.latitude());
        }
        if (request.longitude() != null) {
            this.longitude = BigDecimal.valueOf(request.longitude());
        }
        if (request.pricePerMonth() != null) {
            this.pricePerMonth = request.pricePerMonth();
        }
    }
}