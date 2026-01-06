package kr.java.java.domain.auth.service;

import kr.java.java.domain.auth.dto.LoginRequest;
import kr.java.java.domain.auth.dto.OAuth2UserInfo;
import kr.java.java.domain.auth.dto.TokenResponse;
import kr.java.java.domain.auth.entity.RefreshToken;
import kr.java.java.domain.auth.jwt.JwtTokenProvider;
import kr.java.java.domain.auth.repository.RefreshTokenRepository;
import kr.java.java.domain.user.entity.Provider;
import kr.java.java.domain.user.entity.Role;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // OAuth2 로그인 처리
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = super.loadUser(userRequest);

        // Provider 정보 추출
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        // OAuth2UserInfo 생성
        OAuth2UserInfo userInfo = OAuth2UserInfo.of(registrationId, attributes);

        // 사용자 조회 또는 생성
        User user = saveOrUpdate(userInfo);

        return new kr.java.java.domain.auth.security.CustomOAuth2User(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                registrationId.toUpperCase(),
                attributes
        );
    }

    // 사용자 저장 또는 업데이트
    @Transactional
    public User saveOrUpdate(OAuth2UserInfo userInfo) {
        Provider provider = Provider.valueOf(userInfo.getProvider());

        User user = userRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
                .map(entity -> {
                    // 기존 사용자 정보 업데이트
                    entity.updateProfile(userInfo.getName(), userInfo.getProfileImage());
                    return entity;
                })
                .orElseGet(() -> {
                    // 신규 사용자 생성 (기본 MAKER 역할)
                    return User.builder()
                            .email(userInfo.getEmail())
                            .nickname(userInfo.getName())
                            .profileImageUrl(userInfo.getProfileImage())
                            .provider(provider)
                            .providerId(userInfo.getProviderId())
                            .role(Role.MAKER)  // 기본값
                            .build();
                });

        return userRepository.save(user);
    }

    // JWT 토큰 발급
    @Transactional
    public TokenResponse issueToken(Long userId) {
        // Access Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(userId);

        // Refresh Token 생성
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        // Refresh Token DB 저장 (기존 토큰 있으면 업데이트)
        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        token -> token.updateToken(refreshToken, LocalDateTime.now().plusDays(7)),
                        () -> {
                            RefreshToken newToken = RefreshToken.builder()
                                    .userId(userId)
                                    .token(refreshToken)
                                    .expiresAt(LocalDateTime.now().plusDays(7))
                                    .build();
                            refreshTokenRepository.save(newToken);
                        }
                );

        return TokenResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpirationInSeconds()
        );
    }

    // Refresh Token으로 Access Token 재발급
    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken) {
        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // DB에서 Refresh Token 조회
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Refresh Token입니다."));

        // 만료 확인
        if (token.isExpired()) {
            throw new IllegalArgumentException("만료된 Refresh Token입니다.");
        }

        // 새 Access Token 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(token.getUserId());

        return TokenResponse.of(
                newAccessToken,
                refreshToken,  // Refresh Token은 그대로
                jwtTokenProvider.getAccessTokenExpirationInSeconds()
        );
    }

    // 로그아웃 (Refresh Token 삭제)
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}