package kr.java.java.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.java.java.domain.user.entity.Provider;
import kr.java.java.domain.user.entity.Role;
import kr.java.java.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MypageResponse {

    private ProfileInfo profile;
    private StatsInfo stats;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileInfo {
        @JsonProperty("userId")
        private UUID uuid;


        private String nickname;
        private String email;
        private String profileImageUrl;
        private Provider provider;
        private Role role;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatsInfo {
        private Long spacesCount;
        private Long portfoliosCount;
        private Long reviewsCount;
        private Long likesCount;
    }

    public static MypageResponse of(User user, StatsInfo stats) {
        ProfileInfo profile = ProfileInfo.builder()
                .uuid(user.getUuid())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .provider(user.getProvider())
                .role(user.getRole())
                .build();

        return MypageResponse.builder()
                .profile(profile)
                .stats(stats)
                .build();
    }
}