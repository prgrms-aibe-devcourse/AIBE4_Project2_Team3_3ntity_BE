package kr.java.java.domain.matching.event;

import java.util.UUID;

public record MatchingExpiredEvent (
    Long spaceId,
    UUID senderId,
    String senderNickname,
    UUID receiverId,
    String receiverNickname
){}
