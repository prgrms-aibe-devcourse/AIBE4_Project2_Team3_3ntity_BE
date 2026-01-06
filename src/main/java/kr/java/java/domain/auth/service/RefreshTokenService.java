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

    // Refresh Token 저장 (Redis)
    public void saveRefreshToken(UUID uuid, String refreshToken) {
        String key = "RT:" + uuid.toString();
        redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    // Refresh Token 조회 (Redis)
    public String getRefreshToken(UUID uuid) {
        String key = "RT:" + uuid.toString();
        return redisTemplate.opsForValue().get(key);
    }

    // Refresh Token 삭제 (로그아웃)
    public void deleteRefreshToken(UUID uuid) {
        String key = "RT:" + uuid.toString();
        redisTemplate.delete(key);
    }

    // Refresh Token 검증 (Redis의 토큰과 일치하는지)
    public boolean validateRefreshToken(UUID uuid, String refreshToken) {
        String storedToken = getRefreshToken(uuid);
        return storedToken != null && storedToken.equals(refreshToken);
    }
}
