package kr.java.java.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentAnswerRequest(
        @NotBlank(message = "답변 내용을 입력해주세요.")
        String answer
) {}
