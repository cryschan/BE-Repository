package io.github.cryschan.berepository._global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정 클래스
 * 임시로 Swagger UI 접근을 위한 기본 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Swagger UI 접근 경로
     */
    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    /**
     * 인증이 필요 없는 공개 API 경로
     */
    private static final String[] PUBLIC_WHITELIST = {
            "/api/v1/auth/**",
            "/api/v1/faqs/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (REST API이므로)
                .csrf(AbstractHttpConfigurer::disable)
                
                // 세션 사용 안함 (JWT 사용 예정)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // 경로별 인증 설정
                .authorizeHttpRequests(auth -> auth
                        // Swagger UI 접근 허용
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
                        // 공개 API 접근 허용
                        .requestMatchers(PUBLIC_WHITELIST).permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                );
        
        return http.build();
    }
}