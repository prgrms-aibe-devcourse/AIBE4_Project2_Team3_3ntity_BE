package kr.java.java.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class UserResponse {

    @JsonIgnore
    private Long id;

    @JsonIgnore
    private UUID uuid;

    private String email;
    private String nickname;
    private String profileImageUrl;
    private Provider provider;
    private Role role;

    // User 엔티티를 DTO로 변환
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .provider(user.getProvider())
                .role(user.getRole())
                .build();
    }
}
