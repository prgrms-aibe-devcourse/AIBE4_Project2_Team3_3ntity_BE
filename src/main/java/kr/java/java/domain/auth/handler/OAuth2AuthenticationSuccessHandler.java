package kr.java.java.domain.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.java.java.domain.auth.jwt.JwtTokenProvider;
import kr.java.java.domain.auth.security.CustomOAuth2User;
import kr.java.java.domain.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.frontend.url:http://localhost:8081}")
    private String frontendUrl;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
        UUID uuid = oauth2User.getUuid();
        
        String sameSitePolicy = determineSameSitePolicy(request);
        
        log.info("OAuth2 Success - UUID: {}, Frontend URL: {}, Cookie Secure: {}, SameSite: {}", 
                uuid, frontendUrl, cookieSecure, sameSitePolicy);

        String refreshToken = jwtTokenProvider.createRefreshToken(uuid);
        refreshTokenService.saveRefreshToken(uuid, refreshToken);
        
        log.info("Refresh Token created and saved: {}", refreshToken.substring(0, 20) + "...");

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .maxAge(7 * 24 * 60 * 60)
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(sameSitePolicy)
                .build();
        
        log.info("Setting cookie: {}", refreshCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        String targetUrl = frontendUrl + "/auth/oauth2-callback";
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    private String determineSameSitePolicy(HttpServletRequest request) {
        // CORS 환경에서는 None을 사용, 동일 도메인에서는 Lax 사용
        if (isCrossDomain(request)) {
            return "None"; // 크로스 도메인
        } else {
            return "Lax"; // 동일 도메인
        }
    }
    
    private boolean isCrossDomain(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        String origin = request.getHeader("Origin");
        String serverName = request.getServerName();

        // 프론트엔드 URL과 백엔드 도메인 비교
        try {
            String frontendHost = java.net.URLDecoder.decode(frontendUrl, "UTF-8");
            if (frontendHost.startsWith("http://")) {
                frontendHost = frontendHost.substring(7);
            } else if (frontendHost.startsWith("https://")) {
                frontendHost = frontendHost.substring(8);
            }
            frontendHost = frontendHost.split("/")[0]; // 경로 부분 제거

            return !frontendHost.equals(serverName);
        } catch (Exception e) {
            log.warn("Failed to determine cross-domain, defaulting to None", e);
            return true; // 안전하게 크로스 도메인으로 가정
        }
    }
}