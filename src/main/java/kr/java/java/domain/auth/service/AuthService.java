package kr.java.java.domain.auth.service;

import kr.java.java.domain.auth.dto.OAuth2UserInfo;
import kr.java.java.domain.auth.dto.TokenResponse;
import kr.java.java.domain.auth.exception.AuthErrorCode;
import kr.java.java.domain.auth.exception.AuthException;
import kr.java.java.domain.auth.jwt.JwtTokenProvider;
import kr.java.java.domain.user.entity.Provider;
import kr.java.java.domain.user.entity.Role;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import kr.java.java.global.util.ProfileImageUrlGenerator;
import kr.java.java.global.util.RandomNicknameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

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
        if (userInfo == null || userInfo.getProviderId() == null) {
            throw new AuthException(AuthErrorCode.INVALID_USER_INFO);
        }

        Provider provider;
        try {
            provider = Provider.valueOf(userInfo.getProvider());
        } catch (IllegalArgumentException e) {
            throw new AuthException(AuthErrorCode.INVALID_PROVIDER);
        }

        User user = userRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
                .map(entity -> { //

                    return entity;
                })
                .orElseGet(() -> {

                    UUID uuid = UUID.randomUUID();

                    if (userInfo.getName() == null || userInfo.getName().isEmpty()) {
                        throw new AuthException(AuthErrorCode.MISSING_REQUIRED_FIELD);
                    }
                    return User.builder()
                            .uuid(uuid)
                            .email(userInfo.getEmail())
                            .nickname(RandomNicknameGenerator.generate())
                            .profileImageUrl(ProfileImageUrlGenerator.generate(uuid))
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

        refreshTokenService.saveRefreshToken(uuid, refreshToken);

        return TokenResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpirationInSeconds()
        );
    }

    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new kr.java.java.domain.auth.exception.AuthException(
                    kr.java.java.domain.auth.exception.AuthErrorCode.INVALID_TOKEN
            );
        }

        UUID uuid = jwtTokenProvider.getUuidFromToken(refreshToken);

        if (!refreshTokenService.validateRefreshToken(uuid, refreshToken)) {
            refreshTokenService.deleteRefreshToken(uuid);
            throw new kr.java.java.domain.auth.exception.AuthException(
                    kr.java.java.domain.auth.exception.AuthErrorCode.TOKEN_REUSE_DETECTED
            );
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(uuid);

        return TokenResponse.of(
                newAccessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpirationInSeconds()
        );
    }

    @Transactional
    public void logout(UUID uuid) {
        refreshTokenService.deleteRefreshToken(uuid);
    }
}