# Swagger/OpenAPI 문서화 가이드

## 📋 Quick Start
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/v3/api-docs
- **Spring Boot**: 3.5.7 | **SpringDoc**: 2.7.0

## 🗂️ 프로젝트 구조

### 설정 파일 위치
- **SwaggerConfig**: `src/main/java/io/github/cryschan/berepository/_global/config/SwaggerConfig.java`
- **SecurityConfig**: `src/main/java/io/github/cryschan/berepository/_global/config/SecurityConfig.java`
- **Dependencies**: `build.gradle` → `springdoc-openapi-starter-webmvc-ui:2.7.0`
- **Configuration**: `application.yml` → `springdoc.swagger-ui.path`

### 현재 컨트롤러 상태
| Controller | 경로 | Swagger 적용 | 설명 |
|------------|------|------------|------|
| LoginController | `/api/v1/auth/**` | ✅ 적용 | 로그인/회원가입 |
| ProfileController | `/api/v1/users/**` | ❌ 미적용 | 사용자 프로필 |
| BlogTemplateController | `/api/blog-templates/**` | ❌ 미적용 | 블로그 템플릿 |
| MusinsaController | `/api/keyword/**` | ❌ 미적용 | 패션 키워드 |
| AiController | `/api/v1/ai/**` | ❌ 미적용 | AI 서비스 |

## ✅ 개발 체크리스트

### 새 Controller 작성 시
- [ ] 클래스에 `@Tag` 추가
- [ ] 각 메서드에 `@Operation` 추가
- [ ] Swagger UI에서 동작 확인
- [ ] SecurityConfig에 공개 API 경로 추가 (필요시)

### 필수 어노테이션
```java
@Tag(name = "도메인명", description = "상세 설명")
@RestController
public class YourController {
    
    @Operation(summary = "간단 설명", description = "상세 설명")
    @PostMapping("/path")
    public Response method(@RequestBody Request request) {
        // ...
    }
}
```

## 📌 실제 적용 예시

### ✅ 완료: LoginController
```java
@Tag(name = "인증", description = "로그인/회원가입 API")
@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    @PostMapping("/signup")
    public UserResponse signup(@RequestBody LoginRequest request) {}
}
```

### 📝 TODO: BlogTemplateController
```java
@Tag(name = "블로그 템플릿", description = "블로그 템플릿 관리 API")
@RestController
@RequestMapping("/api/blog-templates")
public class BlogTemplateController {
    @Operation(summary = "템플릿 생성")
    @PostMapping
    public ResponseEntity<BlogTemplateResponse> createTemplate() {}
    
    @Operation(summary = "템플릿 조회")
    @GetMapping("/{templateId}")
    public BlogTemplateResponse getTemplate() {}
}
```

### 📝 TODO: MusinsaController
```java
@Tag(name = "패션 키워드", description = "무신사 매거진 키워드 API")
@RestController
@RequestMapping("/api/keyword")
public class MusinsaController {
    @Operation(summary = "인기 패션 키워드 조회")
    @GetMapping("/fashion")
    public List<KeywordResponseDto> magazine() {}
}
```

## 🔒 Security 설정
- **Swagger 경로**: 인증 없이 접근 가능 (이미 설정됨)
- **공개 API**: `/api/v1/auth/**`, `/api/v1/faqs/**`
- **인증 필요**: 그 외 모든 경로

자세한 설정은 `SecurityConfig.java` 참조

## 🚨 주의사항
- **프로덕션 환경**: Swagger UI 비활성화 권장
- **민감한 정보**: Request/Response 예제에 실제 데이터 노출 주의
- **버전 관리**: API 버전 변경 시 Swagger 문서도 함께 업데이트

## 📚 참고 자료
- [SpringDoc 공식 문서](https://springdoc.org/)
- [OpenAPI 3.0 Specification](https://swagger.io/specification/)