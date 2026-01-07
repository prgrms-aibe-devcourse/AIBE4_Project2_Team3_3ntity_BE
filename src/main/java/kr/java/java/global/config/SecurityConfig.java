package kr.java.java.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.java.java.domain.auth.filter.JwtAuthenticationFilter;
//import kr.java.java.domain.auth.handler.OAuth2AuthenticationFailureHandler;
import kr.java.java.domain.auth.jwt.JwtTokenProvider;
import kr.java.java.domain.auth.service.AuthService;
import kr.java.java.domain.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7일

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 보안 헤더 추가
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/piece/spaces/**").permitAll()
                        .requestMatchers("/piece/portfolios/**").permitAll()
                        .requestMatchers("/piece/reviews/**").permitAll()
                        .requestMatchers("/piece/matchings/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/auth/**", "/login/**", "/oauth2/**", "/piece/auths/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(authService)
                        )
//                        .failureHandler(oAuth2AuthenticationFailureHandler)
                        .successHandler((request, response, authentication) -> {

                            kr.java.java.domain.auth.security.CustomOAuth2User oauth2User =
                                    (kr.java.java.domain.auth.security.CustomOAuth2User) authentication.getPrincipal();

                            UUID uuid = oauth2User.getUuid();

                            String accessToken = jwtTokenProvider.createAccessToken(uuid);
                            String refreshToken = jwtTokenProvider.createRefreshToken(uuid);

                            refreshTokenService.saveRefreshToken(uuid, refreshToken);

                            // Refresh Token을 Cookie에 저장 (HttpOnly로 XSS 방지)
                            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                                    .maxAge(REFRESH_TOKEN_MAX_AGE)
                                    .path("/")
                                    .httpOnly(true)
                                    .secure(cookieSecure)
                                    .sameSite("Lax")
                                    .build();

                            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");

                            // Access Token을 JSON 응답 본문에 포함 (프론트엔드 메모리 저장용)
                            Map<String, Object> result = new HashMap<>();
                            result.put("accessToken", accessToken);
                            result.put("uuid", uuid.toString());
                            result.put("message", "로그인 성공");

                            response.getWriter().write(objectMapper.writeValueAsString(result));
                        })
                );

        return http.build();
    }
}