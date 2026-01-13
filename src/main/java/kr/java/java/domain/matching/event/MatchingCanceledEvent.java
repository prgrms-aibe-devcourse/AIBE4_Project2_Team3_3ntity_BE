package kr.java.java.domain.matching.event;

import java.util.UUID;

public record MatchingCanceledEvent(
        UUID receiverId,
        String senderNickname,
        String relatedUrl
){}
