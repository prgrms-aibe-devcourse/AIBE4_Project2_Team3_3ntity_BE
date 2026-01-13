package kr.java.java.domain.matching.event;

import java.util.UUID;

public record MatchingExpiredEvent (
    Long spaceId,
    UUID senderId,
    UUID senderUuid,
    String senderNickname,
    UUID receiverId,
    UUID receiverUuid,
    String receiverNickname
){}
