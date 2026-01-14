package kr.java.java.domain.space.dto;

import kr.java.java.domain.space.entity.Space;

public record AiSpaceListResponse(
        Long spaceId,
        String title,
        String thumbnailUrl,
        String address,
        Integer pricePerMonth,
        String category,
        String aiReason // 🔥 AI 추천 멘트
) {
    // Entity -> DTO 변환을 위한 정적 팩토리 메소드 (선택 사항이지만 추천)
    public static AiSpaceListResponse of(Space space, String thumbnailUrl, String aiReason) {
        return new AiSpaceListResponse(
                space.getId(),
                space.getTitle(),
                thumbnailUrl,
                space.getAddress(),
                space.getPricePerMonth(),
                space.getCategory().getDescription(), // Enum의 한글 설명
                aiReason
        );
    }
}