package kr.java.java.domain.image.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageDomain {
    USER("user"),
    SPACE("space"),
    REVIEW("review"),
    PORTFOLIO("portfolio");

    private final String dirName;
}
