package kr.java.java.domain.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import kr.java.java.domain.auth.exception.AuthErrorCode;
import kr.java.java.domain.auth.exception.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // Access Token 생성
    public String createAccessToken(UUID uuid) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(uuid.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(UUID uuid) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(uuid.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    // 토큰에서 userId 추출
    public UUID getUuidFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            if (subject == null || subject.isEmpty()) {
                throw new AuthException(AuthErrorCode.INVALID_TOKEN);
            }

            return UUID.fromString(subject);
        } catch (JwtException e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Access Token 만료 시간 반환 (초 단위)
    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration / 1000;
    }
}