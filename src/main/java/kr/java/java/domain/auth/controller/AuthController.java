package kr.java.java.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.java.java.domain.auth.dto.TokenResponse;
import kr.java.java.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/piece/auths")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Refresh Token으로 Access Token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestHeader("Authorization") String refreshToken) {
        String token = refreshToken.replace("Bearer ", "");
        TokenResponse response = authService.refreshAccessToken(token);
        return ResponseEntity.ok(response);
    }

    // 소셜 로그인 테스트
    @GetMapping("/login-success")
    public ResponseEntity<String> loginSuccess() {
        return ResponseEntity.ok("소셜 로그인 성공!");
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("User-Uuid") UUID uuid) {
        authService.logout(uuid);
        return ResponseEntity.ok().build();
    }

}
