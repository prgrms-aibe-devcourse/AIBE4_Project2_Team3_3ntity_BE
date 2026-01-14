package kr.java.java.domain.space.dto;

import kr.java.java.domain.space.enums.SpaceCategory;

public record SpaceMatchingFormResponse(
        Long spaceId,
        String title,
        String thumnailImageUrl,
        String address,
        String detailAddress,
        Double averageRating,
        Integer reviewCount,
        SpaceCategory category,
        Integer pricePerMonth
) {}
