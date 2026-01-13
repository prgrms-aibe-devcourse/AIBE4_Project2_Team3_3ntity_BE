package kr.java.java.domain.matching.event;

public record MatchingAcceptedEvent(
        Long receiverId,
        String senderNickname,
        String relatedUrl
){}
