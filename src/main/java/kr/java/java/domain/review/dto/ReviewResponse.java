package kr.java.java.domain.review.dto;

import kr.java.java.domain.review.entity.Review;
import java.time.LocalDateTime;

public record ReviewResponse(
        Long reviewId,
        Long matchingId,
        Long writerId,
        String writerNickname,
        Integer rating,
        String content,
        LocalDateTime createdAt
) {
    // Entity -> DTO 변환 메서드
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getMatching().getId(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
