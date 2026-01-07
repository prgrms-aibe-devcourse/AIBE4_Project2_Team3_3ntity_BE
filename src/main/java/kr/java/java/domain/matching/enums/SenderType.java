package kr.java.java.domain.matching.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SenderType {
    HOST("공간 소유주"),
    USER("일반 유저");

    private final String description;
}
