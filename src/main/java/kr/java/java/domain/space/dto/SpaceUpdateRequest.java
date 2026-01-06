package kr.java.java.domain.space.dto;

public record SpaceUpdateRequest(
        String title,
        String description,
        String category,
        String address,
        String detailAddress,
        Double latitude,
        Double longitude,
        Integer pricePerMonth
) {}