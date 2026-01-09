package kr.java.java.domain.space.dto;

import kr.java.java.domain.space.enums.SpaceStatus;

public record SpaceUpdateRequest(
        String title,
        String description,
        String category,
        String address,
        String detailAddress,
        Double latitude,
        Double longitude,
        Integer pricePerMonth,
        SpaceStatus status
) {}