package kr.java.java.domain.matching.dto;

import kr.java.java.domain.matching.entity.Matching;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MatchingResponse(
        Long matchingId,
        String spaceTitle,
        String mainImageUrl,
        String opponentNickname,
        String category,
        Integer pricePerMonth,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        LocalDateTime createdAt
) {
    public static MatchingResponse from(Matching matching, Long UserId, String mainImageUrl) {
        String opponentNickname = matching.getUser().getId().equals(UserId)
                ? matching.getReceiver().getNickname() : matching.getUser().getNickname();

        return new MatchingResponse(
                matching.getId(),
                matching.getSpace().getTitle(),
                mainImageUrl,
                opponentNickname,
                matching.getSpace().getCategory().getDescription(),
                matching.getSpace().getPricePerMonth(),
                matching.getStartDate(),
                matching.getEndDate(),
                matching.getStatus().getDescription(),
                matching.getCreatedAt()
        );
    }
}
