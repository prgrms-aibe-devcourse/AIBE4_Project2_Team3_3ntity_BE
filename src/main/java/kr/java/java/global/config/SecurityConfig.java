package kr.java.java.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import kr.java.java.domain.auth.filter.JwtAuthenticationFilter;
import kr.java.java.domain.auth.jwt.JwtTokenProvider;
import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.auth.service.AuthService;
import kr.java.java.domain.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
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

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/piece/spaces/**").permitAll()
                        .requestMatchers("/piece/reviews/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/auth/**", "/login/**", "/oauth2/**", "/piece/auths/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(authService)
                        )
                        .successHandler((request, response, authentication) -> {

                            kr.java.java.domain.auth.security.CustomOAuth2User oauth2User =
                                    (kr.java.java.domain.auth.security.CustomOAuth2User) authentication.getPrincipal();

                            UUID uuid = oauth2User.getUuid();

                            String accessToken = jwtTokenProvider.createAccessToken(uuid);
                            String refreshToken = jwtTokenProvider.createRefreshToken(uuid);

                            refreshTokenService.saveRefreshToken(uuid, refreshToken);

                            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                                    .maxAge(7 * 24 * 60 * 60)
                                    .path("/")
                                    .httpOnly(true)
                                    .secure(false)
                                    .sameSite("Lax")
                                    .build();

                            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");

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