package kr.java.java.domain.auth.service;

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
import java.util.UUID;

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

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        OAuth2UserInfo userInfo = OAuth2UserInfo.of(registrationId, attributes);

        User user = saveOrUpdate(userInfo);

        return new kr.java.java.domain.auth.security.CustomOAuth2User(
                user.getUuid(),
                user.getEmail(),
                user.getRole(),
                registrationId.toUpperCase(),
                attributes
        );
    }

    @Transactional
    public User saveOrUpdate(OAuth2UserInfo userInfo) {
        Provider provider = Provider.valueOf(userInfo.getProvider());

        User user = userRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
                .map(entity -> {
                    entity.updateProfile(userInfo.getName(), userInfo.getProfileImage());
                    return entity;
                })
                .orElseGet(() -> {
                    return User.builder()
                            .email(userInfo.getEmail())
                            .nickname(userInfo.getName())
                            .profileImageUrl(userInfo.getProfileImage())
                            .provider(provider)
                            .providerId(userInfo.getProviderId())
                            .role(Role.USER)
                            .build();
                });

        return userRepository.save(user);
    }

    @Transactional
    public TokenResponse issueToken(UUID uuid) {

        String accessToken = jwtTokenProvider.createAccessToken(uuid);

        String refreshToken = jwtTokenProvider.createRefreshToken(uuid);

        refreshTokenRepository.findByUserUuid(uuid)
                .ifPresentOrElse(
                        token -> token.updateToken(refreshToken, LocalDateTime.now().plusDays(7)),
                        () -> {
                            RefreshToken newToken = RefreshToken.builder()
                                    .userUuid(uuid)
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

    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Refresh Token입니다."));

        if (token.isExpired()) {
            throw new IllegalArgumentException("만료된 Refresh Token입니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(token.getUserUuid());

        return TokenResponse.of(
                newAccessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpirationInSeconds()
        );
    }

    @Transactional
    public void logout(UUID uuid) {
        refreshTokenRepository.deleteByUserUuid(uuid);
    }
}