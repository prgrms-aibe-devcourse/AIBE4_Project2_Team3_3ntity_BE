package kr.java.java.domain.matching.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateMatchingCommand(
        Long spaceId,
        UUID senderUuid,
        UUID receiverUuid,
        String message,
        LocalDate startDate,
        Integer months
) {}