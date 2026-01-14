package kr.java.java.domain.user.dto;

import kr.java.java.domain.user.entity.User;
import lombok.Builder;

@Builder
public record UserMatchingFormResponse(
        Long userId,
        String nickname,
        String profileImageUrl
) {
    public static UserMatchingFormResponse from(User user) {
        return UserMatchingFormResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
