package kr.java.java.domain.space.dto;

import kr.java.java.domain.space.entity.Space;

import java.math.BigDecimal;

public record SpaceListResponse(
        Long id,
        String title,
        String category,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer pricePerMonth,
        String status
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
                space.getStatus().name()
        );
    }
}