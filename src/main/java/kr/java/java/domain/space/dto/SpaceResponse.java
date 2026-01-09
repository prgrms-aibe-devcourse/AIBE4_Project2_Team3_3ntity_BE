package kr.java.java.domain.space.dto;

import kr.java.java.domain.space.entity.Space;

import java.time.LocalDateTime;

public record SpaceResponse(
        Long id,
        String title,
        String description,
        String category,
        String address,
        String detailAddress,
        Double latitude,
        Double longitude,
        Integer pricePerMonth,
        String status,
        LocalDateTime createdAt,
        String hostName
) {
    // Entity를 DTO로 변환하는 생성자 (편의를 위해 추가)
    public SpaceResponse(Space space) {
        this(
                space.getId(),
                space.getTitle(),
                space.getDescription(),
                space.getCategory(),
                space.getAddress(),
                space.getDetailAddress(),
                space.getLatitude() != null ? space.getLatitude().doubleValue() : null,
                space.getLongitude() != null ? space.getLongitude().doubleValue() : null,
                space.getPricePerMonth(),
                space.getStatus().name(),
                space.getCreatedAt(),
                space.getUser().getNickname()
        );
    }
}