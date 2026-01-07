package kr.java.java.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ReviewUpdateRequest(
        Long userId,

        String content,

        @Min(value = 1, message = "별점은 1점 이상")
        @Max(value = 5, message = "별점은 5점 이하")
        Integer rating
) {}