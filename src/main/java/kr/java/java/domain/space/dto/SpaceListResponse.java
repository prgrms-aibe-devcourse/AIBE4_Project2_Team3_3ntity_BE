package kr.java.java.domain.space.dto;

import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.enums.SpaceCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SpaceListResponse(
        Long id,
        String title,
        SpaceCategory category,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer pricePerMonth,
        String status,
        LocalDateTime createdAt
) {
    public SpaceListResponse(Space space) {
        this(
                space.getId(),
                space.getTitle(),
                space.getCategory(),
                space.getAddress(),
                space.getLatitude(),
                space.getLongitude(),
                space.getPricePerMonth(),
                space.getStatus().name(),
                space.getCreatedAt()
        );
    }
}