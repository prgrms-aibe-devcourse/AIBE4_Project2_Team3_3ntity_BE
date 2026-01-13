package kr.java.java.domain.matching.event;

public record MatchingExpiredEvent (
    Long spaceId,
    Long senderId,
    String senderNickname,
    Long receiverId,
    String receiverNickname
){}
