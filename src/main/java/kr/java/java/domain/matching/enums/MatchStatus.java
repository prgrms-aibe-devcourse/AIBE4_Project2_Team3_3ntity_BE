package kr.java.java.domain.matching.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MatchStatus {

    WAITING("대기중"),
    REJECTED("거절됨"),
    ONGOING("진행중"),
    COMPLETED("완료됨"),
    CANCELLED("취소됨");

    private final String description;
}