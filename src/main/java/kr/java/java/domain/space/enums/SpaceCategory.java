package kr.java.java.domain.space.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum SpaceCategory {
    CAFE("카페"),
    RESTAURANT("식당"),
    BEAUTY("뷰티/미용"),
    STUDIO("스튜디오"),
    OFFICE("공유오피스"),
    STORE("상점/매장"),
    ETC("기타");

    private final String description; // 한글 설명 (검색용)

    public static SpaceCategory findByDescription(String keyword) {
        return Arrays.stream(values())
                .filter(c -> c.description.equals(keyword))
                .findFirst()
                .orElse(null);
    }
}
