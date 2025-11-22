package io.github.cryschan.berepository._global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 3.0 설정 클래스
 * JWT Bearer 토큰 인증을 지원하는 Swagger UI 구성
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // JWT 보안 스키마 정의
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT Bearer 토큰을 입력하세요. (Bearer 접두사는 자동 추가됨)");

        // Security 요구사항 정의
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme));
    }

    private Info apiInfo() {
        return new Info()
                .title("BE Repository API")
                .description("""
                        BE Repository REST API 문서

                        ## 인증 방법
                        1. 먼저 `/api/auth/login` 으로 로그인하여 JWT 토큰을 받습니다.
                        2. 상단의 'Authorize' 버튼을 클릭합니다.
                        3. 받은 JWT 토큰을 입력합니다. (Bearer 접두사는 자동으로 추가됩니다)
                        4. 이제 인증이 필요한 API를 테스트할 수 있습니다.

                        ## 테스트 계정
                        ### 👤 일반 사용자 (USER)
                        - 이메일: user@repository.com
                        - 비밀번호: 1234qwer
                        - 권한: 일반 사용자 기능 (조회, 등록 등)

                        ### 👑 관리자 (ADMIN)
                        - 이메일: admin@repository.com
                        - 비밀번호: 1234qwer
                        - 권한: 관리자 기능 (전체 관리, 삭제 등)

                        ## 사용자 역할
                        - **USER**: 일반 사용자 권한. 기본적인 CRUD 기능을 사용할 수 있습니다.
                        - **ADMIN**: 관리자 권한. 모든 기능에 접근 가능하며, 사용자 관리 및 시스템 설정을 수정할 수 있습니다.
                        """)
                .version("1.0.0");
    }

    private List<Server> serverList() {
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local Development Server");

        Server prodServer = new Server()
                .url("https://api.be-repository.com")
                .description("Production Server");

        return List.of(localServer, prodServer);
    }
}