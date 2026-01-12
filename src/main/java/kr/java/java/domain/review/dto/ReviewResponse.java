package kr.java.java.domain.review.dto;

import kr.java.java.domain.image.dto.ImageResponse;
import kr.java.java.domain.review.entity.Review;
import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        Long reviewId,
        Long matchingId,
        Long writerId,
        String writerNickname,
        Integer rating,
        String content,
        LocalDateTime createdAt,
        List<ImageResponse> images
) {
    // Entity -> DTO 변환 메서드
    public static ReviewResponse of(Review review, List<ImageResponse> images) {
        return new ReviewResponse(
                review.getId(),
                review.getMatching().getId(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                images
        );
    }
}
