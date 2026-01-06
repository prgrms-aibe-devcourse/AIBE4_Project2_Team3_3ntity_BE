package kr.java.java.domain.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import kr.java.java.domain.auth.dto.TokenResponse;
import kr.java.java.domain.auth.exception.AuthErrorCode;
import kr.java.java.domain.auth.exception.AuthException;
import kr.java.java.domain.auth.jwt.JwtTokenProvider;
import kr.java.java.domain.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/piece/auths")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(HttpServletRequest request) {

        String refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            throw new AuthException(AuthErrorCode.TOKEN_NOT_FOUND);
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        UUID uuid = jwtTokenProvider.getUuidFromToken(refreshToken);

        if (!refreshTokenService.validateRefreshToken(uuid, refreshToken)) {
            refreshTokenService.deleteRefreshToken(uuid);
            throw new AuthException(AuthErrorCode.TOKEN_REUSE_DETECTED);
        }

        refreshTokenService.deleteRefreshToken(uuid);

        String newAccessToken = jwtTokenProvider.createAccessToken(uuid);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(uuid);

        refreshTokenService.saveRefreshToken(uuid, newRefreshToken);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .maxAge(7 * 24 * 60 * 60)
                .path("/")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .build();

        TokenResponse response = TokenResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtTokenProvider.getAccessTokenExpirationInSeconds()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    @GetMapping("/login-success")
    public ResponseEntity<String> loginSuccess() {
        return ResponseEntity.ok("소셜 로그인 성공!");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {

        String refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            UUID uuid = jwtTokenProvider.getUuidFromToken(refreshToken);
            refreshTokenService.deleteRefreshToken(uuid);
        }

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(false)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("로그아웃 성공");
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}