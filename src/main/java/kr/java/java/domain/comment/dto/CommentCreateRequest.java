package kr.java.java.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentCreateRequest(
        @NotNull(message = "사용자 ID 필수")
        Long userId,

        Long spaceId,
        Long portfolioId,

        @NotBlank(message = "문의 내용 필수")
        String content,

        boolean isSecret
) {}
