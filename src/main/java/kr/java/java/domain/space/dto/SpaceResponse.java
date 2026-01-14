package kr.java.java.domain.space.dto;

import kr.java.java.domain.image.dto.ImageResponse;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.enums.SpaceCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SpaceResponse(
        Long id,
        String title,
        String description,
        SpaceCategory category,
        String address,
        String detailAddress,
        Double latitude,
        Double longitude,
        Integer pricePerMonth,
        String status,
        LocalDateTime createdAt,
        String hostName,
        List<ImageResponse>images,
        UUID hostUuid
) {
    // Entity를 DTO로 변환하는 생성자 (편의를 위해 추가)
    public static SpaceResponse of(Space space, List<ImageResponse> images) {
        return new SpaceResponse(
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
                space.getUser().getNickname(),
                images,
                space.getUser().getUuid()
        );
    }
}