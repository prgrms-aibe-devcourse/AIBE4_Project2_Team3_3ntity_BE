package kr.java.java.domain.comment.dto;

import kr.java.java.domain.comment.entity.Comment;
import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        Long spaceId,
        Long portfolioId,
        String content,
        String writerNickname,
        String writerImage,
        boolean isSecret,
        boolean isMine,
        boolean isHost,
        String answer,
        LocalDateTime answeredAt,
        LocalDateTime createdAt
) {
    // viewerId: 조회하는 사람의 ID
    public static CommentResponse of(Comment comment, Long viewerId) {
        // 1. 작성자 본인인지 확인
        boolean isWriter = viewerId != null && comment.getUser().getId().equals(viewerId);

        // 2. 공간(또는 포트폴리오) 주인인지 확인
        boolean isOwner = false;
        Long spaceId = null;
        Long portfolioId = null;

        if (comment.getSpace() != null) {
            // 공간 주인인지 체크
            if (viewerId != null) {
                isOwner = comment.getSpace().getUser().getId().equals(viewerId);
            }
            spaceId = comment.getSpace().getId();
        } else if (comment.getPortfolio() != null) {
            // 포트폴리오 주인인지 체크
            if (viewerId != null) {
                isOwner = comment.getPortfolio().getUser().getId().equals(viewerId);
            }
            portfolioId = comment.getPortfolio().getId();
        }

        // 3. 비밀글 처리 (내용 & 답변 가리기)
        String contentToSend = comment.getContent();
        String answerToSend = comment.getAnswer();

        // 비밀글이고 (작성자도 아니고 주인도 아니면) 내용을 가림
        if (comment.isSecret() && !isWriter && !isOwner) {
            contentToSend = "비밀글입니다.";

            // 답변이 존재한다면 답변도 같이 가림
            if (answerToSend != null && !answerToSend.isEmpty()) {
                answerToSend = "비밀글입니다.";
            }
        }

        return new CommentResponse(
                comment.getId(),
                spaceId,
                portfolioId,
                contentToSend,
                comment.getUser().getNickname(),
                comment.getUser().getProfileImageUrl(),
                comment.isSecret(),
                isWriter,
                isOwner,
                answerToSend,
                comment.getAnsweredAt(),
                comment.getCreatedAt()
        );
    }
}
