package kr.java.java.domain.auth.service;

import kr.java.java.domain.auth.exception.AuthErrorCode;
import kr.java.java.domain.auth.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_KEY_PREFIX = "RT:";
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "BL:";
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60; // 7일 (초)

    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(UUID uuid, String refreshToken) {
        if (uuid == null || refreshToken == null || refreshToken.isEmpty()) {
            throw new AuthException(AuthErrorCode.INVALID_INPUT);
        }
        String key = REFRESH_TOKEN_KEY_PREFIX + uuid.toString();
        redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public String getRefreshToken(UUID uuid) {
        if (uuid == null) {
            throw new AuthException(AuthErrorCode.INVALID_INPUT);
        }
        String key = REFRESH_TOKEN_KEY_PREFIX + uuid.toString();
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(UUID uuid) {
        if (uuid == null) {
            throw new AuthException(AuthErrorCode.INVALID_INPUT);
        }
        String key = REFRESH_TOKEN_KEY_PREFIX + uuid.toString();
        redisTemplate.delete(key);
    }

    public boolean validateRefreshToken(UUID uuid, String refreshToken) {
        String storedToken = getRefreshToken(uuid);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    public boolean validateAndRotate(UUID uuid, String oldRefreshToken) {
        String storedToken = getRefreshToken(uuid);

        if (storedToken == null || !storedToken.equals(oldRefreshToken)) {
            deleteRefreshToken(uuid);
            return false;
        }

        return true;
    }

    /**
     * Access Token을 블랙리스트에 추가 (로그아웃 시 사용)
     * @param accessToken 블랙리스트에 추가할 Access Token
     * @param expirationTime 만료 시간 (초 단위)
     */
    public void addAccessTokenToBlacklist(String accessToken, long expirationTime) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new AuthException(AuthErrorCode.INVALID_INPUT);
        }
        String key = ACCESS_TOKEN_BLACKLIST_PREFIX + accessToken;
        redisTemplate.opsForValue().set(key, "blacklisted", expirationTime, TimeUnit.SECONDS);
    }

    /**
     * Access Token이 블랙리스트에 있는지 확인
     * @param accessToken 확인할 Access Token
     * @return 블랙리스트에 있으면 true
     */
    public boolean isAccessTokenBlacklisted(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            return false;
        }
        String key = ACCESS_TOKEN_BLACKLIST_PREFIX + accessToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
