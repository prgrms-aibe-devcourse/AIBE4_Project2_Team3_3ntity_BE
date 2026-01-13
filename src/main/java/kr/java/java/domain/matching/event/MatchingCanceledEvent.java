package kr.java.java.domain.matching.event;

public record MatchingCanceledEvent(
        Long receiverId,
        String senderNickname,
        String relatedUrl
){}
