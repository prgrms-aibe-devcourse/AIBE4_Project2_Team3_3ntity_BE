package kr.java.java.domain.space.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpaceStatus {
    RECRUITING("구인 중"),
    CLOSED("구인 종료");

    private final String description;
}
