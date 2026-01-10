package kr.java.java.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.java.java.domain.auth.filter.JwtAuthenticationFilter;
import kr.java.java.domain.auth.handler.OAuth2AuthenticationFailureHandler;
import kr.java.java.domain.auth.handler.OAuth2AuthenticationSuccessHandler;
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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
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
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final ObjectMapper objectMapper;
    private final CorsProperties corsProperties;

    @org.springframework.beans.factory.annotation.Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url:http://localhost:8081}")
    private String frontendUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; img-src 'self' data: https://api.dicebear.com;"))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(request -> "OPTIONS".equals(request.getMethod())).permitAll()
                        .requestMatchers("/").permitAll() // 루트 경로 허용
                        .requestMatchers("/api/auth/**", "/login/**", "/oauth2/**", "/piece/auths/**", "/error").permitAll()
                        .requestMatchers("/piece/spaces/**").permitAll()
                        .requestMatchers("/piece/portfolios/**").permitAll()
                        .requestMatchers("/piece/reviews/**").permitAll()
                        .requestMatchers("/piece/comments/**").permitAll()
                        .requestMatchers("/piece/favorites/**").permitAll()
                        .requestMatchers("/piece/matchings/**").permitAll()
                        .requestMatchers("/piece/images/**").permitAll()
                        .requestMatchers("/piece/notifications/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/piece/auths/logout").authenticated()
                        .requestMatchers("/piece/mypages/**").authenticated().anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new AuthenticationEntryPoint() {
                            @Override
                            public void commence(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 AuthenticationException authException) throws IOException, ServletException {

                                if ("OPTIONS".equals(request.getMethod())) {
                                    response.setStatus(HttpServletResponse.SC_OK);
                                    return;
                                }

                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                response.setCharacterEncoding("UTF-8");

                                Map<String, Object> result = new HashMap<>();
                                result.put("code", "UNAUTHORIZED");
                                result.put("message", "인증이 필요합니다.");

                                response.getWriter().write(objectMapper.writeValueAsString(result));
                            }
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(authService)
                        )
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                );

        return http.build();
    }
}