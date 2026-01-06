package kr.java.java.domain.auth.controller;

import kr.java.java.domain.auth.dto.TokenResponse;
import kr.java.java.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/piece/auths")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Refresh Token으로 Access Token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestHeader("Authorization") String refreshToken) {
        // "Bearer " 제거
        String token = refreshToken.replace("Bearer ", "");
        TokenResponse response = authService.refreshAccessToken(token);
        return ResponseEntity.ok(response);
    }

    // 소셜 로그인 테스트
    @GetMapping("/login-success")
    public ResponseEntity<String> loginSuccess(@RequestParam Long userId) {
        return ResponseEntity.ok("소셜 로그인 성공! 유저 ID: " + userId);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("User-Id") Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok().build();
    }
}
