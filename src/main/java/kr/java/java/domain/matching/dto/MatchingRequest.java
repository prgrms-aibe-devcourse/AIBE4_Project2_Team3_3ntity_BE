package kr.java.java.domain.matching.dto;

public record MatchingRequest(
        Long spaceId,
        Long userId,
        String message
) {
}
