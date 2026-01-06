package kr.java.java.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60; // 7일 (초)

    public void saveRefreshToken(UUID uuid, String refreshToken) {
        String key = "RT:" + uuid.toString();
        redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public String getRefreshToken(UUID uuid) {
        String key = "RT:" + uuid.toString();
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(UUID uuid) {
        String key = "RT:" + uuid.toString();
        redisTemplate.delete(key);
    }

    public boolean validateRefreshToken(UUID uuid, String refreshToken) {
        String storedToken = getRefreshToken(uuid);
        return storedToken != null && storedToken.equals(refreshToken);
    }
}
