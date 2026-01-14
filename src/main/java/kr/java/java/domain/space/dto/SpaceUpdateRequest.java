package kr.java.java.domain.space.dto;

import kr.java.java.domain.space.enums.SpaceCategory;
import kr.java.java.domain.space.enums.SpaceStatus;

import java.util.List;

public record SpaceUpdateRequest(
        String title,
        String description,
        SpaceCategory category,
        String address,
        String detailAddress,
        Double latitude,
        Double longitude,
        Integer pricePerMonth,
        SpaceStatus status,
        List<Long> remainImageIds
) {}