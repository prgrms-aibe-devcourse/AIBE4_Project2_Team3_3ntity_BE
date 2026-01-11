package kr.java.java.domain.notification.enums;

public enum NotificationType {
    MATCHING("매칭신청"),
    COMMENT("문의"),
    CONTRACT_END("계약종료"),
    MATCHING_COMPLETE("매칭완료"),
    MATCHING_CANCELED("매칭취소"),
    MATCHING_REJECT("매칭거절"),
    ETC("기타");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
