package kr.java.java.domain.matching.event;

public record MatchingRejectedEvent(
        Long receiverId,
        String senderNickname,
        String relatedUrl
){}
