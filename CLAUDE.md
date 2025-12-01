# BE-Repository - Claude Code 설정

## 프로젝트 개요

데이터 수집 및 AI 분석 시스템 백엔드 (Spring Boot 3.5.7 + Java 21)

## 기술 스택

- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.5.7, Spring Security, Spring Data JPA
- **Database**: PostgreSQL 16
- **AI**: Spring AI 1.0.0-M4 (OpenAI)
- **Build**: Gradle 8.x
- **API Docs**: SpringDoc OpenAPI (Swagger UI)
- **Auth**: JWT (jjwt 0.12.5)
- **Crawling**: Jsoup 1.18.3

## 프로젝트 구조

```
src/main/java/io/github/cryschan/berepository/
├── _global/                    # 전역 설정 및 공통 컴포넌트
│   ├── config/                 # Security, Swagger, RestClient 설정
│   ├── exception/              # 전역 예외 처리 (BaseException, ErrorCode)
│   └── jwt/                    # JWT 인증 필터 및 유틸리티
└── domain/                     # 도메인별 패키지 (DDD 스타일)
    ├── ai/                     # AI 분석 기능
    ├── blog/                   # 블로그 관리
    ├── blogtemplate/           # 블로그 템플릿 (스케줄러 포함)
    ├── dashboard/              # 대시보드 (스케줄러 포함)
    ├── faqs/                   # FAQ 관리
    ├── fashion/                # 패션 크롤러 (무신사, 싸다구)
    └── user/                   # 사용자 인증/관리
```

## 도메인 구조 패턴

각 도메인은 다음 구조를 따름:
```
domain/{name}/
├── controller/     # REST API 엔드포인트
├── service/        # 비즈니스 로직
├── repository/     # JPA Repository
├── entity/         # JPA Entity
├── dto/
│   ├── request/    # 요청 DTO
│   └── response/   # 응답 DTO
├── exception/      # 도메인별 예외 (DomainException 상속)
├── crawler/        # 크롤러 (해당 시)
└── scheduler/      # 스케줄러 (해당 시)
```

## 코딩 컨벤션

### 네이밍
- Controller: `{Domain}Controller`
- Service: `{Domain}Service`
- Repository: `{Domain}Repository`
- Entity: 단수형 (`User`, `Blog`)
- DTO: `{Domain}{Action}Request/Response` (예: `LoginRequest`, `BlogResponse`)
- Exception: `{Domain}Exception`

### 예외 처리
- 전역: `GlobalExceptionHandler`에서 처리
- 도메인별 예외는 `DomainException` 상속
- `ErrorCode` enum으로 에러 코드 관리

### API 응답
- Swagger/OpenAPI 어노테이션 사용 (`@Operation`, `@ApiResponse`)
- 표준 응답 형식 사용

## 빌드 및 실행

```bash
# 로컬 개발 (PostgreSQL Docker)
docker-compose up -d postgres
./gradlew bootRun

# 테스트
./gradlew test

# 빌드
./gradlew clean build
```

## 환경 변수

`.env` 파일 필요 (`.env.example` 참고):
- `DB_*`: PostgreSQL 연결 정보
- `JWT_*`: JWT 설정 (secret, expiration)
- `OPENAI_*`: OpenAI API 설정

## API 문서

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health Check: `http://localhost:8080/actuator/health`

## 주의사항

- `.env` 파일은 Git에서 제외됨 (보안)
- Docker 이미지는 non-root 사용자로 실행
- `bootJar`만 생성 (plain.jar 비활성화)
