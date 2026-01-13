package kr.java.java.domain.comment.event;

import java.util.UUID;

public record CommentCreatedEvent(
        UUID receiverId,
        String senderNickname,
        String relatedUrl
){}
