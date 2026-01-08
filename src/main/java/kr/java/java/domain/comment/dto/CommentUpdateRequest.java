package kr.java.java.domain.comment.dto;

public record CommentUpdateRequest(
        String content,
        Boolean isSecret
) {
}
