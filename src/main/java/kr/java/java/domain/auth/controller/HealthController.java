package kr.java.java.domain.auth.controller;

import kr.java.java.domain.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/piece/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final RefreshTokenService refreshTokenService;

    @GetMapping("/redis")
    public ResponseEntity<String> checkRedisConnection() {
        try {
            UUID testUuid = UUID.randomUUID();
            String testToken = "test-token-" + System.currentTimeMillis();
            
            // Redis 저장 테스트
            refreshTokenService.saveRefreshToken(testUuid, testToken);
            
            // Redis 조회 테스트
            String retrievedToken = refreshTokenService.getRefreshToken(testUuid);
            
            // Redis 삭제
            refreshTokenService.deleteRefreshToken(testUuid);
            
            boolean success = testToken.equals(retrievedToken);
            
            log.info("Redis connection test: {}", success ? "SUCCESS" : "FAILED");
            
            return ResponseEntity.ok(success ? "Redis connection: OK" : "Redis connection: FAILED");
            
        } catch (Exception e) {
            log.error("Redis connection test failed", e);
            return ResponseEntity.status(500).body("Redis connection error: " + e.getMessage());
        }
    }

    @GetMapping("/auth-info")
    public ResponseEntity<String> getAuthInfo() {
        try {
            // 현재 환경 설정 정보 로깅
            log.info("=== Auth Configuration Info ===");
            log.info("Cookie Secure: {}", System.getProperty("app.cookie.secure", "NOT SET"));
            log.info("Frontend URL: {}", System.getProperty("app.frontend.url", "NOT SET"));
            log.info("Redis Host: {}", System.getProperty("REDIS_HOST", "NOT SET"));
            log.info("Redis SSL Enabled: {}", System.getProperty("REDIS_SSL_ENABLED", "NOT SET"));
            
            return ResponseEntity.ok("Auth configuration logged");
            
        } catch (Exception e) {
            log.error("Failed to get auth info", e);
            return ResponseEntity.status(500).body("Failed to get auth info: " + e.getMessage());
        }
    }
}