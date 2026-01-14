package kr.java.java.domain.space.dto;

public record AiRecommendationDto(
        Long spaceId,
        String reason // AI 추천 멘트
) {}