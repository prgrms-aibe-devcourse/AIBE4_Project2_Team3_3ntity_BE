package kr.java.java.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(

        @NotNull(message = "매칭 ID 필수")
        Long matchingId,

        @NotNull(message = "작성자 ID 필수")
        Long userId,

        @NotNull(message = "별점 필수")
        @Min(value = 1, message = "별점은 최소 1점")
        @Max(value = 5, message = "별점은 최대 5점")
        Integer rating,

        @NotBlank(message = "리뷰 내용 필수")
        String content
) {
}
