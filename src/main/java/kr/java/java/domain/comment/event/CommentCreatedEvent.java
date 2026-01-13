package kr.java.java.domain.comment.event;

public record CommentCreatedEvent(
        Long receiverId,
        String senderNickname,
        String relatedUrl
){}
