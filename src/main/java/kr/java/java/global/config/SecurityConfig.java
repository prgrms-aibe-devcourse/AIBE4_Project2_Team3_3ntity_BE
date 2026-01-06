package kr.java.java.global.config;

import jakarta.servlet.http.HttpServletResponse;
import kr.java.java.domain.auth.entity.RefreshToken;
import kr.java.java.domain.auth.filter.JwtAuthenticationFilter;
import kr.java.java.domain.auth.jwt.JwtTokenProvider;
import kr.java.java.domain.auth.repository.RefreshTokenRepository;
import kr.java.java.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

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
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // OAuth2 로그인 관련 URL 허용
                        .requestMatchers("/api/auth/**", "/login/**", "/oauth2/**", "/login-success", "/piece/spaces/**","/piece/auths/**", "/error").permitAll()                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

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

                            UUID uuid = oauth2User.getUuid();

                            String accessToken = jwtTokenProvider.createAccessToken(uuid);
                            String refreshToken = jwtTokenProvider.createRefreshToken(uuid);

                            // 만료 시간 설정 (예: 7일)
                            LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);

                            RefreshToken tokenEntity = refreshTokenRepository.findByUserUuid(uuid)
                                    .map(existingToken -> {
                                        existingToken.updateToken(refreshToken, expiryDate);
                                        return existingToken;
                                    })
                                    .orElseGet(() -> RefreshToken.builder()
                                            .userUuid(uuid)
                                            .token(refreshToken)
                                            .expiresAt(expiryDate)
                                            .build());

                            refreshTokenRepository.save(tokenEntity);

                            // Refresh Token
                            ResponseCookie cookie = getRefreshTokenCookie(refreshToken);
                            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                            // Redis 저장 (사용자 UUID를 키로)
                            // redisTemplate.opsForValue().set(uuid.toString(), refreshToken, 7, TimeUnit.DAYS);

                            // 최종 리다이렉트 (발급된 accessToken을 파라미터로 전달)
                            String targetUrl = "http://localhost:8080/piece/auths/login-success";

                            response.sendRedirect(targetUrl);                  })



                )

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout") // 로그아웃 요청
                        .logoutSuccessHandler((request, response, authentication) -> {

                            ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                                    .path("/")
                                    .maxAge(0) // 즉시 만료
                                    .httpOnly(true)
                                    .secure(false)
                                    .build();

                            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                                    .path("/")
                                    .maxAge(0)
                                    .httpOnly(true)
                                    .secure(false)
                                    .build();

                            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
                            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

                            // 3. 로그아웃 성공 후 이동할 페이지
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("Logout Success");
                        })
                );

        return http.build();
    }



    private ResponseCookie getRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .maxAge(7 * 24 * 60 * 60) // 7일
                .path("/")
                .httpOnly(true)
                .secure(false)
                // .secure(true)
                .sameSite("Lax")
                // .sameSite("None")
                .build();
    }
}
