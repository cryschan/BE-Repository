# BE-Repository 프로젝트 구조

## 📌 프로젝트 개요

### 프로젝트명
**BE-Repository** - 무신사 패션 매거진 데이터 수집 및 AI 분석 시스템

### 프로젝트 목적
무신사 패션 매거진 API에서 데이터를 수집하고, Spring AI(OpenAI)를 활용한 분석 및 요약을 제공하는 백엔드 시스템

### 주요 기능
1. **무신사 데이터 수집**: 패션 매거진 API 연동 및 데이터 크롤링
2. **AI 분석** (예정): OpenAI API를 활용한 콘텐츠 분석
3. **사용자 관리** (예정): 사용자 인증 및 권한 관리
4. **게시판** (예정): 커뮤니티 기능

---

## 🛠️ 기술 스택

### 언어 및 프레임워크
- **Java**: 21 (LTS)
- **Spring Boot**: 3.5.7
- **Spring Framework**: 6.x

### 주요 의존성
- **Spring Web**: RESTful API 구현
- **Spring Data JPA**: 데이터베이스 연동
- **Spring AI**: OpenAI 통합 (1.0.0-M4)
- **Spring Actuator**: 헬스 체크 및 모니터링
- **PostgreSQL**: 관계형 데이터베이스
- **Lombok**: 보일러플레이트 코드 제거
- **Validation**: 데이터 검증

### 빌드 도구
- **Gradle**: 8.14.3

### 인프라
- **Docker**: Multi-stage build로 최적화
- **Docker Compose**: PostgreSQL + Spring Boot 오케스트레이션
- **PostgreSQL**: 16-alpine

---

## 📁 현재 디렉토리 구조

```
BE-Repository/
├── src/
│   ├── main/
│   │   ├── java/io/github/cryschan/berepository/
│   │   │   ├── domain/
│   │   │   │   ├── fashion/          ✅ 구현 완료
│   │   │   │   │   ├── controller/
│   │   │   │   │   │   └── MusinsaController.java
│   │   │   │   │   ├── service/
│   │   │   │   │   │   └── MagazineService.java
│   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── request/
│   │   │   │   │   │   └── response/
│   │   │   │   │   │       ├── MagazineResponseDto.java
│   │   │   │   │   │       ├── KeywordListResponseDto.java
│   │   │   │   │   │       └── KeywordResponseDto.java
│   │   │   │   │   ├── repository/
│   │   │   │   │   └── entity/
│   │   │   │   ├── ai/               🚧 디렉토리만 생성됨
│   │   │   │   │   ├── controller/
│   │   │   │   │   ├── service/
│   │   │   │   │   ├── repository/
│   │   │   │   │   ├── entity/
│   │   │   │   │   └── dto/
│   │   │   │   ├── board/            🚧 디렉토리만 생성됨
│   │   │   │   │   ├── controller/
│   │   │   │   │   ├── service/
│   │   │   │   │   ├── repository/
│   │   │   │   │   ├── entity/
│   │   │   │   │   └── dto/
│   │   │   │   └── user/             🚧 디렉토리만 생성됨
│   │   │   │       ├── controller/
│   │   │   │       ├── service/
│   │   │   │       ├── repository/
│   │   │   │       ├── entity/
│   │   │   │       └── dto/
│   │   │   └── _global/
│   │   │       ├── client/
│   │   │       │   └── RestClientConfig.java  ✅ 구현 완료
│   │   │       │   # ChatClientConfig.java 제거됨 (Spring AI auto-configuration 사용)
│   │   │       ├── config/
│   │   │       ├── exception/
│   │   │       └── common/
│   │   │           └── dto/
│   │   └── resources/
│   │       └── application.yml       ✅ 구현 완료
│   └── test/
│       └── java/io/github/cryschan/berepository/
├── docs/
│   └── PROJECT_STRUCTURE.md
├── gradle/
│   └── wrapper/
├── Dockerfile                        ✅ 구현 완료
├── docker-compose.yml                ✅ 구현 완료
├── .dockerignore                     ✅ 구현 완료
├── .env.example                      ✅ 구현 완료
├── .gitignore                        ✅ 구현 완료
├── build.gradle                      ✅ 구현 완료
├── settings.gradle
└── gradlew
```

---

## 🏗️ 도메인 레이어 아키텍처

### 설계 철학
도메인 중심 설계(Domain-Driven Design)를 기반으로 각 비즈니스 도메인을 독립적으로 구성

---

### 1. fashion 도메인 ✅ **구현 완료**

**위치**: `domain/fashion/`

**책임**:
- 무신사 패션 매거진 API 연동
- 매거진 데이터 수집 및 처리
- 키워드 기반 콘텐츠 검색

**현재 구현**:
- `controller/MusinsaController.java`: 무신사 데이터 수집 API
  - `GET /api/keyword/fashion`: 패션 키워드 기반 매거진 조회
- `service/MagazineService.java`: 무신사 API 호출 로직
- `dto/response/MagazineResponseDto.java`: 매거진 응답 DTO
- `dto/response/KeywordListResponseDto.java`: 키워드 목록 DTO
- `dto/response/KeywordResponseDto.java`: 개별 키워드 DTO

**API 엔드포인트**:
```
GET /api/keyword/fashion
- 무신사 패션 매거진 인기 콘텐츠 조회
- 카테고리: 001001002 (패션)
- 정렬: CONTENT_POPULARITY_SCORE (인기순)
- 크기: 4개 콘텐츠
```

---

### 2. ai 도메인 🚧 **구조만 생성됨**

**위치**: `domain/ai/`

**예정 책임**:
- AI 관련 기능 통합 관리
- OpenAI API 연동
- 텍스트 요약 및 분석
- AI 프롬프트 관리

**구성**:
- `controller/`: AI 관련 API 엔드포인트 (미구현)
- `service/`: AI 모델 호출 및 처리 로직 (미구현)
- `repository/`: AI 설정 및 이력 저장 (미구현)
- `entity/`: AI 관련 엔티티 (미구현)
- `dto/`: AI 요청/응답 DTO (미구현)

---

### 3. board 도메인 🚧 **구조만 생성됨**

**위치**: `domain/board/`

**예정 책임**:
- 게시판 기능
- 게시글 CRUD 작업
- 댓글 및 첨부파일 관리
- 게시글 검색 및 필터링

**구성**:
- `controller/`: 게시판 API 엔드포인트 (미구현)
- `service/`: 게시글 관리 로직 (미구현)
- `repository/`: 게시글 데이터 저장 (미구현)
- `entity/`: 게시글 엔티티 (미구현)
- `dto/`: 게시판 요청/응답 DTO (미구현)

---

### 4. user 도메인 🚧 **구조만 생성됨**

**위치**: `domain/user/`

**예정 책임**:
- 사용자 관리
- 인증 및 권한 관리
- 사용자 프로필 관리
- 사용자 활동 이력 추적

**구성**:
- `controller/`: 사용자 관리 API 엔드포인트 (미구현)
- `service/`: 사용자 비즈니스 로직 (미구현)
- `repository/`: 사용자 데이터 저장 (미구현)
- `entity/`: 사용자 엔티티 (미구현)
- `dto/`: 사용자 요청/응답 DTO (미구현)

---

## 🌐 Global 공통 레이어

### 위치
`_global/`

### 구성

#### 1. client/ ✅ **구현 완료**
**역할**: 외부 API 클라이언트 설정

**구현된 클래스**:
- `RestClientConfig.java`: RestClient Bean 설정
  - Base URL: `https://content.musinsa.com`
  - 타임아웃: 10초
  - JSON 자동 처리

---

#### 2. config/ 🚧 **구조만 생성됨**
**역할**: 전역 설정 클래스

**예정 설정**:
- JPA/Hibernate 설정
- Security 설정 (필요시)
- CORS 설정 (필요시)

---

#### 3. exception/ 🚧 **구조만 생성됨**
**역할**: 전역 예외 처리

**예정 클래스**:
- `GlobalExceptionHandler`: @RestControllerAdvice를 통한 중앙 예외 처리
- 커스텀 예외 클래스

---

#### 4. common/dto/ 🚧 **구조만 생성됨**
**역할**: 도메인 간 공통 사용 DTO

**예정 DTO**:
- `ApiResponse<T>`: 표준 API 응답 래퍼
- `ErrorResponse`: 에러 응답 표준화

---

## 📦 의존성 상세

### Spring Boot Starters
```gradle
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-validation'
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

### Spring AI
```gradle
implementation 'org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-M4'
```

### Database
```gradle
runtimeOnly 'org.postgresql:postgresql'
```

### Lombok
```gradle
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

### Test
```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```

---

## ⚙️ 설정 파일

### application.yml
**위치**: `src/main/resources/application.yml`

**주요 설정**:
- Spring 애플리케이션 이름: `be-repository`
- PostgreSQL 데이터소스 설정
  - 기본 DB 이름: `test_db`
- JPA/Hibernate 설정 (DDL auto: update)
- OpenAI API 설정
  - 모델: `gpt-3.5-turbo` (기본값)
  - Temperature: 0.7
- 서버 포트: 8080
- 로깅 레벨: DEBUG (기본값)
- Actuator: Spring Boot 기본 설정 사용 (/actuator/health)

**환경 변수 사용**:
모든 민감 정보는 환경 변수로 관리 (`${변수명:기본값}` 형식)

---

### .env.example
**위치**: `.env.example`

**목적**: 환경 변수 템플릿

**포함 항목**:
```env
# OpenAI Configuration
OPENAI_API_KEY=your-openai-api-key-here
OPENAI_MODEL=gpt-3.5-turbo
OPENAI_TEMPERATURE=0.7

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=test_db
DB_USERNAME=postgres
DB_PASSWORD=postgres

# JPA Configuration
DDL_AUTO=update
SHOW_SQL=true

# Server Configuration
SERVER_PORT=8080
LOG_LEVEL=DEBUG
```

**사용법**:
```bash
cp .env.example .env
# .env 파일을 열어서 실제 값으로 수정
```

---

### Dockerfile ✅ **Multi-stage Build**
**위치**: `Dockerfile`

**특징**:
- **Stage 1 (Builder)**: Gradle 8 + JDK 21 Alpine
  - Gradle 의존성 캐싱
  - bootJar 빌드 (테스트 스킵)
- **Stage 2 (Runtime)**: Eclipse Temurin 21 JRE Alpine
  - Non-root user (spring:spring) 보안
  - Health check 내장
  - 최적화된 JVM 파라미터

**이미지 크기**: ~200MB (Multi-stage 덕분)

---

### docker-compose.yml ✅ **완전 구현**
**위치**: `docker-compose.yml`

**서비스**:

#### 1. postgres
- 이미지: `postgres:16-alpine`
- 포트: 5432
- 볼륨: `postgres_data` (영구 저장)
- 헬스 체크: `pg_isready`
- 네트워크: `final-network`

#### 2. app
- 빌드: Dockerfile 기반
- 포트: 8080
- 의존성: PostgreSQL 헬스 체크 완료 후 시작
- 환경 변수: .env 파일 사용
- 헬스 체크: `/actuator/health`
- 네트워크: `final-network`

**실행**:
```bash
# PostgreSQL만 실행
docker-compose up -d postgres

# 전체 실행 (PostgreSQL + Spring Boot)
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 종료
docker-compose down
```

---

## 🚀 프로젝트 실행 가이드

### 방법 1: 로컬 실행 (개발 환경)

#### 1. 사전 준비
- Java 21 설치
- Docker 설치

#### 2. 저장소 클론
```bash
git clone https://github.com/cryschan/BE-Repository.git
cd BE-Repository
```

#### 3. 환경 변수 설정
```bash
cp .env.example .env
# .env 파일에서 OPENAI_API_KEY 등 실제 값으로 수정
# DB_HOST=localhost로 설정
```

#### 4. PostgreSQL 실행 (Docker)
```bash
docker-compose up -d postgres
```

#### 5. 애플리케이션 실행
```bash
./gradlew bootRun
```

#### 6. 접속 확인
```
http://localhost:8080/api/keyword/fashion
http://localhost:8080/actuator/health
```

---

### 방법 2: Docker 전체 실행 (프로덕션 환경)

#### 1. 환경 변수 설정
```bash
cp .env.example .env
# .env 파일에서 실제 값으로 수정
# DB_HOST=postgres로 설정 (Docker 네트워크용)
```

#### 2. Docker Compose 실행
```bash
docker-compose up -d
```

#### 3. 로그 확인
```bash
docker-compose logs -f app
```

#### 4. 접속 확인
```
http://localhost:8080/api/keyword/fashion
http://localhost:8080/actuator/health
```

#### 5. 종료
```bash
docker-compose down
```

---

## 📝 구현 우선순위 (다음 단계)

### 우선순위 1: Global 공통 레이어 완성
- [ ] `GlobalExceptionHandler` - 전역 예외 처리
- [ ] `ApiResponse<T>` - 표준 응답 DTO
- [ ] `ErrorResponse` - 에러 응답 DTO

### 우선순위 2: Fashion 도메인 완성
- [ ] `FashionProduct` 엔티티 설계
- [ ] `FashionProductRepository` 구현
- [ ] 매거진 데이터 저장 기능
- [ ] 키워드 검색 기능 확장

### 우선순위 3: AI 도메인 구현
- [ ] OpenAI 서비스 연동
- [ ] 텍스트 요약 API
- [ ] AI 요약 결과 저장

### 우선순위 4: User 도메인 구현
- [ ] User 엔티티 설계
- [ ] 회원가입/로그인 API
- [ ] Spring Security 연동

### 우선순위 5: Board 도메인 구현
- [ ] Board 엔티티 설계
- [ ] 게시글 CRUD API
- [ ] 댓글 기능

### 우선순위 6: 테스트
- [ ] Unit Tests
- [ ] Integration Tests
- [ ] API Documentation (Swagger/Spring REST Docs)

---

## 🔒 보안 고려사항

### .gitignore 설정 ✅
민감 정보가 포함된 파일들은 Git 추적 제외:
- `.env` - OpenAI API 키 보호
- `application-local.yml`
- `application-secret.yml`
- 로그 파일 (`*.log`)
- 데이터베이스 파일 (`*.db`, `*.sqlite`)
- 인증서 파일 (`*.pem`, `*.key`, `*.jks`)

### 환경 변수 관리 ✅
- 모든 API 키와 비밀번호는 환경 변수로 관리
- `.env.example`로 필요한 변수 템플릿 제공
- 실제 `.env` 파일은 Git에 커밋하지 않음

### Docker 보안 ✅
- Non-root user로 애플리케이션 실행 (spring:spring)
- Multi-stage build로 빌드 도구 제외
- Health check로 안정성 확보

---

## 📊 프로젝트 현황

### ✅ 완료된 작업
- ✅ 프로젝트 초기 설정
- ✅ 도메인 레이어 구조 설계 (4개 도메인)
- ✅ 의존성 설정 (Spring AI, JPA, PostgreSQL, Actuator 등)
- ✅ application.yml 작성
- ✅ Docker Multi-stage build 구현
- ✅ Docker Compose 설정 (PostgreSQL + App)
- ✅ .gitignore 보안 설정
- ✅ Fashion 도메인 구현 (musinsa 패키지에서 이동)
  - MusinsaController
  - MagazineService
  - 3개 Response DTO
- ✅ RestClientConfig 구현
- ✅ 무신사 API 연동 테스트 성공

### 🚧 진행 중인 작업
- 🚧 AI 도메인 (디렉토리 생성됨, 구현 대기)
- 🚧 Board 도메인 (디렉토리 생성됨, 구현 대기)
- 🚧 User 도메인 (디렉토리 생성됨, 구현 대기)

### ⏳ 예정된 작업
- ⏳ Global 공통 레이어 구현
- ⏳ Entity 설계 및 구현
- ⏳ Service 로직 구현
- ⏳ 테스트 코드 작성

---

## 📚 참고 문서

### Spring AI 공식 문서
- https://docs.spring.io/spring-ai/reference/

### Spring Boot 3.5.x 문서
- https://docs.spring.io/spring-boot/docs/3.5.x/reference/html/

### PostgreSQL 문서
- https://www.postgresql.org/docs/16/

### Docker 문서
- https://docs.docker.com/

---

## 📞 Repository 정보

### GitHub
https://github.com/cryschan/BE-Repository

### Package
`io.github.cryschan.berepository`

### 메인 브랜치
`main`

---

**마지막 업데이트**: 2025-11-19
**문서 버전**: 2.1.0
**프로젝트 상태**: 초기 개발 단계 (Fashion 도메인 구현 완료, 설정 최적화 완료)
