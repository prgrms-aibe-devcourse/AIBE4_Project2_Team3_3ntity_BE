package kr.java.java.domain.space.dto;

import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.enums.SpaceCategory;
import kr.java.java.domain.user.entity.User;

import java.math.BigDecimal;

public record SpaceRequest(
        String title,
        String description,
        SpaceCategory category,
        String address,
        String detailAddress,
        Double latitude,
        Double longitude,
        Integer pricePerMonth
) {
    public Space toEntity(User user) {
        return Space.builder()
                .title(this.title())
                .description(this.description())
                .category(this.category())
                .address(this.address())
                .detailAddress(this.detailAddress())
                .latitude(BigDecimal.valueOf(this.latitude()))
                .longitude(BigDecimal.valueOf(this.longitude()))
                .pricePerMonth(this.pricePerMonth())
                .user(user)
                .build();
    }
}
