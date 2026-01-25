package kr.java.java.domain.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.java.java.domain.auth.jwt.JwtTokenProvider;
import kr.java.java.domain.auth.security.CustomOAuth2User;
import kr.java.java.domain.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
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
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.frontend.url:http://localhost:8081}")
    private String frontendUrl;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    String sameSitePolicy = cookieSecure ? "None" : "Lax";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
        UUID uuid = oauth2User.getUuid();

        String refreshToken = jwtTokenProvider.createRefreshToken(uuid);
        refreshTokenService.saveRefreshToken(uuid, refreshToken);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .maxAge(7 * 24 * 60 * 60)
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(sameSitePolicy)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        String targetUrl = frontendUrl + "/auth/oauth2-callback";
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}