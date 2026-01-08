package kr.java.java.domain.matching.dto;

import java.time.LocalDate;

public record CreateMatchingCommand(
        Long spaceId,
        Long senderId,
        Long receiverId,
        String message,
        LocalDate startDate,
        Integer months
) {}