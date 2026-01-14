package kr.java.java.domain.matching.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record UserMatchingFormResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        List<MySpaceSummary> mySpaces
) {}