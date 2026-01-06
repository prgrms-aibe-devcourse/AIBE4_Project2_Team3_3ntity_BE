package kr.java.java.global.config;

import kr.java.java.domain.auth.entity.RefreshToken;
import kr.java.java.domain.auth.jwt.JwtAuthenticationFilter;
import kr.java.java.domain.auth.jwt.JwtTokenProvider;
import kr.java.java.domain.auth.repository.RefreshTokenRepository;
import kr.java.java.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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
                        .requestMatchers("/piece/matchings/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // OAuth2 로그인 관련 URL 허용
                        .requestMatchers("/api/auth/**", "/login/**", "/oauth2/**", "/login-success", "/piece/spaces/**","/piece/auths/**").permitAll()                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(authService)
                        )
                        .successHandler((request, response, authentication) -> {

                            // 로그인 성공 시 처리
                            kr.java.java.domain.auth.security.CustomOAuth2User oauth2User =
                                    (kr.java.java.domain.auth.security.CustomOAuth2User) authentication.getPrincipal();
                            // TODO: JWT 발급 후 프론트로 리다이렉트
                            //response.sendRedirect("http://localhost:8080/user/naver/login?userId=" + oauth2User.getUserId());
                            // })

                            Long userId = oauth2User.getUserId();

                            // JWT 토큰 생성
                            String accessToken = jwtTokenProvider.createAccessToken(userId);
                            String refreshToken = jwtTokenProvider.createRefreshToken(userId);

                            // 만료 시간 설정 (예: 7일)
                            LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);

                            // Refresh Token DB 저장 로직
                            // 기존 토큰이 있으면 업데이트하고, 없으면 새로 빌드합니다.
                            RefreshToken tokenEntity = refreshTokenRepository.findByUserId(userId)
                                    .map(existingToken -> {
                                        existingToken.updateToken(refreshToken, expiryDate);
                                        return existingToken;
                                    })
                                    .orElseGet(() -> RefreshToken.builder()
                                            .userId(userId)
                                            .token(refreshToken)
                                            .expiresAt(expiryDate)
                                            .build());

                            refreshTokenRepository.save(tokenEntity);

                            // 최종 리다이렉트 (발급된 accessToken을 파라미터로 전달)
                            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8080/piece/auths/login-success")
                                    .queryParam("accessToken", accessToken)
                                    .queryParam("userId", userId)
                                    .build().toUriString();

                            response.sendRedirect(targetUrl);                  })
                );

        return http.build();
    }
}
