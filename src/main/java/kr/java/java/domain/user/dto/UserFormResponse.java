package kr.java.java.domain.user.dto;

import kr.java.java.domain.user.entity.User;
import lombok.Builder;

@Builder
public record UserFormResponse(
        Long userId,
        String nickname,
        String profileImageUrl
) {
    public static UserFormResponse from(User user) {
        return UserFormResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
