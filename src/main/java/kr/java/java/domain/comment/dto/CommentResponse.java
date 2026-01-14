package kr.java.java.domain.comment.dto;

import kr.java.java.domain.comment.entity.Comment;
import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        Long spaceId,
        Long portfolioId,
        String content,
        String writerNickname,
        boolean isSecret,
        boolean isMine,
        LocalDateTime createdAt
) {
    // viewerId: 조회하는 사람의 ID
    public static CommentResponse of(Comment comment, Long viewerId) {
        // 1. 작성자 본인인지 확인
        boolean isWriter = comment.getUser().getId().equals(viewerId);

        // 2. 공간(또는 포트폴리오) 주인인지 확인
        boolean isOwner = false;
        Long spaceId = null;
        Long portfolioId = null;
        if (comment.getSpace() != null) {
            isOwner = comment.getSpace().getUser().getId().equals(viewerId);
            spaceId = comment.getSpace().getId();
        } else if (comment.getPortfolio() != null) {
            isOwner = comment.getPortfolio().getUser().getId().equals(viewerId);
            portfolioId = comment.getPortfolio().getId();
        }

        // 3. 작성자도 아니고 주인도 아니면 내용을 가림
        String contentToSend = comment.getContent();
        if (comment.isSecret() && !isWriter && !isOwner) {
            contentToSend = "비밀글입니다.";
        }

        return new CommentResponse(
                comment.getId(),
                spaceId,
                portfolioId,
                contentToSend,
                comment.getUser().getNickname(),
                comment.isSecret(),
                isWriter,
                comment.getCreatedAt()
        );
    }
}
