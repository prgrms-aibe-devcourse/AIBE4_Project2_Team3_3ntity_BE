package kr.java.java.domain.review.dto;

import kr.java.java.domain.image.dto.ImageResponse;
import kr.java.java.domain.matching.entity.Matching;
import kr.java.java.domain.review.entity.Review;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record ReviewResponse(
        Long reviewId,
        Long matchingId,
        Long writerId,
        String writerNickname,
        String spaceTitle,
        String period,
        Integer rating,
        String content,
        LocalDateTime createdAt,
        List<ImageResponse> images
) {
    // Entity -> DTO 변환 메서드
    public static ReviewResponse of(Review review, List<ImageResponse> images) {
        Matching matching = review.getMatching();
        // 날짜 포맷팅 로직
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        String periodString = matching.getStartDate().format(formatter) + " ~ " +
                matching.getEndDate().format(formatter);

        return new ReviewResponse(
                review.getId(),
                review.getMatching().getId(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                matching.getSpace().getTitle(),
                periodString,
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                images
        );
    }
}
