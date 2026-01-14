package kr.java.java.domain.matching.dto;

import lombok.Builder;

@Builder
public record MySpaceSummary(
        Long spaceId,
        String spaceName
) {}