package kr.java.java.domain.auth.dto;

import kr.java.java.domain.auth.exception.AuthErrorCode;
import kr.java.java.domain.auth.exception.AuthException;
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
            default -> throw new AuthException(AuthErrorCode.INVALID_PROVIDER);
        };
    }

    // 카카오 정보 추출
    private static OAuth2UserInfo ofKakao(Map<String, Object> attributes) {
        if (attributes == null || attributes.get("id") == null) {
            throw new AuthException(AuthErrorCode.INVALID_USER_INFO);
        }

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

        String providerId = String.valueOf(attributes.get("id"));
        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
        String nickname = properties != null ? (String) properties.get("nickname") : null;
        String profileImage = properties != null ? (String) properties.get("profile_image") : null;

        if (providerId == null || providerId.isEmpty() || "null".equals(providerId)) {
            throw new AuthException(AuthErrorCode.INVALID_USER_INFO);
        }

        return new OAuth2UserInfo(
                providerId,
                "KAKAO",
                email,
                nickname,
                profileImage
        );
    }

    // 네이버 정보 추출
    private static OAuth2UserInfo ofNaver(Map<String, Object> attributes) {
        if (attributes == null) {
            throw new AuthException(AuthErrorCode.INVALID_USER_INFO);
        }

        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            throw new AuthException(AuthErrorCode.INVALID_USER_INFO);
        }

        String providerId = (String) response.get("id");
        if (providerId == null || providerId.isEmpty()) {
            throw new AuthException(AuthErrorCode.INVALID_USER_INFO);
        }

        return new OAuth2UserInfo(
                providerId,
                "NAVER",
                (String) response.get("email"),
                (String) response.get("name"),
                (String) response.get("profile_image")
        );
    }

    // 구글 정보 추출
    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
        if (attributes == null) {
            throw new AuthException(AuthErrorCode.INVALID_USER_INFO);
        }

        String providerId = (String) attributes.get("sub");
        if (providerId == null || providerId.isEmpty()) {
            throw new AuthException(AuthErrorCode.INVALID_USER_INFO);
        }

        return new OAuth2UserInfo(
                providerId,
                "GOOGLE",
                (String) attributes.get("email"),
                (String) attributes.get("name"),
                (String) attributes.get("picture")
        );
    }
}
