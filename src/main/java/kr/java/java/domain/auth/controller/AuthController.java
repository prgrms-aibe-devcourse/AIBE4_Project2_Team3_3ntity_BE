package kr.java.java.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "인증/인가 API (로그인, 토큰 재발급, 로그아웃)")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7일

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Operation(summary = "OAuth2 토큰 발급", description = "쿠키에 저장된 Refresh Token을 검증하여 초기 Access Token을 발급합니다. (소셜 로그인 직후 호출)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 발급 성공", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰이거나 토큰이 없음", content = @Content(schema = @Schema(hidden = true)))
    })
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

    @Operation(summary = "토큰 재발급 (Reissue)", description = "쿠키의 Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재발급 성공", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh Token이 유효하지 않거나 만료됨 (재로그인 필요)", content = @Content(schema = @Schema(hidden = true)))
    })
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

    @Operation(summary = "로그인 성공 확인", description = "OAuth2 로그인 성공 시 리다이렉트되는 단순 확인용 엔드포인트입니다.")
    @ApiResponse(responseCode = "200", description = "성공 메시지 반환")
    @GetMapping("/login-success")
    public ResponseEntity<String> loginSuccess() {
        return ResponseEntity.ok("소셜 로그인 성공!");
    }

    @Operation(summary = "로그아웃", description = "Refresh Token(DB/쿠키)을 삭제하고 Access Token을 블랙리스트에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "이미 로그아웃 되었거나 유효하지 않은 토큰", content = @Content(schema = @Schema(hidden = true)))
    })
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