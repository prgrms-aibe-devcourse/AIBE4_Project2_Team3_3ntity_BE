package kr.java.java.domain.auth.dto;

import lombok.Getter;
import java.util.Map;

@Getter
public class OAuth2UserInfo {

    private final String providerId;
    private final String provider;
    private final String email;
    private final String name;
    private final String profileImage;

    private OAuth2UserInfo(String providerId, String provider, String email, String name, String profileImage) {
        this.providerId = providerId;
        this.provider = provider;
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
    }

    // Factory 메서드 -> Provider에 맞는 UserInfo 자동 생성
    public static OAuth2UserInfo of(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "kakao" -> ofKakao(attributes);
            case "naver" -> ofNaver(attributes);
            case "google" -> ofGoogle(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 Provider: " + provider);
        };
    }

    // 카카오 정보 추출
    private static OAuth2UserInfo ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

        return new OAuth2UserInfo(
                String.valueOf(attributes.get("id")),
                "KAKAO",
                kakaoAccount != null ? (String) kakaoAccount.get("email") : null,
                properties != null ? (String) properties.get("nickname") : null,
                properties != null ? (String) properties.get("profile_image") : null
        );
    }

    // 네이버 정보 추출
    private static OAuth2UserInfo ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return new OAuth2UserInfo(
                response != null ? (String) response.get("id") : null,
                "NAVER",
                response != null ? (String) response.get("email") : null,
                response != null ? (String) response.get("name") : null,
                response != null ? (String) response.get("profile_image") : null
        );
    }

    // 구글 정보 추출
    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
        return new OAuth2UserInfo(
                (String) attributes.get("sub"),
                "GOOGLE",
                (String) attributes.get("email"),
                (String) attributes.get("name"),
                (String) attributes.get("picture")
        );
    }
}
