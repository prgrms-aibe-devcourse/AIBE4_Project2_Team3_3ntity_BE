package kr.java.java.domain.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import kr.java.java.domain.auth.dto.TokenResponse;
import kr.java.java.domain.auth.exception.AuthErrorCode;
import kr.java.java.domain.auth.exception.AuthException;
import kr.java.java.domain.auth.jwt.JwtTokenProvider;
import kr.java.java.domain.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/piece/auths")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7일

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @GetMapping("/oauth2-token")
    public ResponseEntity<TokenResponse> getOAuth2Token(HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            throw new AuthException(AuthErrorCode.TOKEN_NOT_FOUND);
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        UUID uuid = jwtTokenProvider.getUuidFromToken(refreshToken);

        if (!refreshTokenService.validateRefreshToken(uuid, refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        String accessToken = jwtTokenProvider.createAccessToken(uuid);

        TokenResponse response = TokenResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpirationInSeconds()
        );

        return ResponseEntity.ok().body(response);
    }

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

        ResponseCookie refreshCookie = createRefreshTokenCookie(newRefreshToken);
        
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
        String accessToken = getAccessTokenFromHeader(request);

        // Refresh Token 삭제
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            UUID uuid = jwtTokenProvider.getUuidFromToken(refreshToken);
            refreshTokenService.deleteRefreshToken(uuid);
        }


        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            long remainingTime = jwtTokenProvider.getRemainingExpirationTime(accessToken);
            if (remainingTime > 0) {
                refreshTokenService.addAccessTokenToBlacklist(accessToken, remainingTime);
            }
        }

        ResponseCookie refreshCookie = deleteRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("로그아웃 성공");
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .maxAge(REFRESH_TOKEN_MAX_AGE)
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .build();
    }

    private String getAccessTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(cookieSecure)
                .build();
    }
}