package kr.java.java.domain.matching.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateMatchingToSpaceRequest(
        String message,

        @NotNull(message = "시작 날짜를 선택해주세요.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate startDate,

        @NotNull(message = "계약 기간을 입력해주세요.")
        @Min(value = 1, message = "최소 1개월 이상이어야 합니다.")
        Integer months
) {
}
