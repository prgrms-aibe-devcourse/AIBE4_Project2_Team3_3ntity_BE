package kr.java.java.domain.matching.dto;

import kr.java.java.domain.matching.entity.Matching;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MatchingResponse(
        Long matchingId,
        String spaceTitle,
        String mainImageUrl,
        String opponentNickname,
        Long senderId,
        Long receiverId,
        boolean isReceiver,
        String category,
        String categoryName,
        Integer pricePerMonth,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String statusName,
        LocalDateTime createdAt

) {
    public static MatchingResponse from(Matching matching, Long userId, String mainImageUrl) {
        String opponentNickname = matching.getUser().getId().equals(userId)
                ? matching.getReceiver().getNickname() : matching.getUser().getNickname();

        boolean isReceiver = matching.getReceiver().getId().equals(userId);

        return new MatchingResponse(
                matching.getId(),
                matching.getSpace().getTitle(),
                mainImageUrl,
                opponentNickname,
                matching.getUser().getId(),
                matching.getReceiver().getId(),
                isReceiver,
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
