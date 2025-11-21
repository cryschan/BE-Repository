# Swagger/OpenAPI 문서화 가이드

## 개요
BE-Repository 프로젝트에 SpringDoc OpenAPI를 사용한 간단한 Swagger 설정이 구성되어 있습니다.

## 버전 정보
- Spring Boot: 3.5.7
- SpringDoc OpenAPI: 2.7.0 (Spring Boot 3.5 호환 버전)

## 접속 방법
애플리케이션 실행 후 아래 URL로 접속:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API 문서 (JSON)**: http://localhost:8080/v3/api-docs

## 구성 파일

### 1. build.gradle 의존성
```gradle
// Swagger/OpenAPI 3.0
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'
```

### 2. application.yml 설정
```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

### 3. SwaggerConfig.java
```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BE-Repository API")
                        .version("1.0")
                        .description("Spring Boot REST API 문서"));
    }
}
```

## Controller 어노테이션 사용법

### 기본 어노테이션
```java
// 컨트롤러 레벨 - API 그룹 정의
@Tag(name = "인증", description = "로그인/회원가입 API")

// 메서드 레벨 - 개별 API 설명
@Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
```

### 예제 - LoginController
```java
@Tag(name = "인증", description = "로그인/회원가입 API")
@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {
    
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public UserResponse signup(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.signup(loginRequest);
    }
    
    @Operation(summary = "로그인", description = "사용자 인증을 수행합니다")
    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }
}
```

## 팀 개발 가이드라인

### 필수 작성 항목
1. **@Tag**: Controller 클래스에 반드시 추가
   - `name`: 짧은 이름 (예: "사용자", "게시물")
   - `description`: 상세 설명

2. **@Operation**: 각 API 메서드에 추가
   - `summary`: 간단한 요약 (10자 이내)
   - `description`: 상세 설명 (필요시)

### 간소화 원칙
- 복잡한 @ApiResponse 어노테이션은 생략
- @Schema는 꼭 필요한 경우만 사용
- HTTP 상태 코드는 @ResponseStatus로 명시

## Spring Security 고려사항
현재 Spring Security가 활성화되어 있어 Swagger UI 접속 시 인증이 필요할 수 있습니다.
개발 환경에서는 다음 설정으로 Swagger 경로를 제외할 수 있습니다:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

## 문제 해결

### SpringDoc 버전 호환성
Spring Boot 3.5.x 사용 시 SpringDoc 2.7.0 이상 버전 필요

### 500 에러 발생 시
1. SpringDoc 버전 확인
2. Spring Boot 버전과의 호환성 확인
3. `./gradlew clean build --refresh-dependencies` 실행

## 참고사항
- Swagger UI는 개발/테스트 환경에서만 사용 권장
- 프로덕션 환경에서는 보안을 위해 비활성화 고려