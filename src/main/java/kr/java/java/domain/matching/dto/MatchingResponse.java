package kr.java.java.domain.matching.dto;

import kr.java.java.domain.matching.entity.Matching;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MatchingResponse(
        Long matchingId,
        Long spaceId,
        String spaceTitle,
        String mainImageUrl,
        String opponentNickname,
        UUID senderUuid,
        UUID receiverUuid,
        boolean isReceiver,
        boolean canReview,
        String message,
        String category,
        String categoryName,
        Integer pricePerMonth,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String statusName,
        LocalDateTime createdAt

) {
    public static MatchingResponse from(Matching matching, UUID userUuid, String mainImageUrl, boolean hasReviewd) {
        boolean isReceiver = matching.getReceiver().getUuid().equals(userUuid);
        boolean isHost = matching.getSpace().getUser().getUuid().equals(userUuid);
        boolean canReview = !isHost && !hasReviewd;

        String opponentNickname = isReceiver
                ? matching.getUser().getNickname()
                : matching.getReceiver().getNickname();

        return new MatchingResponse(
                matching.getId(),
                matching.getSpace().getId(),
                matching.getSpace().getTitle(),
                mainImageUrl,
                opponentNickname,
                matching.getUser().getUuid(),
                matching.getReceiver().getUuid(),
                isReceiver,
                canReview,
                matching.getMessage(),
                matching.getSpace().getCategory().name(),
                matching.getSpace().getCategory().getDescription(),
                matching.getSpace().getPricePerMonth(),
                matching.getStartDate(),
                matching.getEndDate(),
                matching.getStatus().name(),
                matching.getStatus().getDescription(),
                matching.getCreatedAt()
        );
    }
}
