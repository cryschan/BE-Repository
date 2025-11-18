# BE-Repository

> 무신사 패션 매거진 데이터 수집 및 AI 분석 시스템

Spring Boot 3.5.7 기반의 백엔드 시스템으로, 무신사 패션 매거진 API에서 데이터를 수집하고 OpenAI를 활용한 콘텐츠 분석 기능을 제공합니다.

## 🚀 주요 기능

- **무신사 데이터 수집**: 패션 매거진 API 연동 및 실시간 데이터 크롤링
- **AI 분석** (예정): OpenAI GPT 모델을 활용한 콘텐츠 요약 및 분석
- **사용자 관리** (예정): 인증 및 권한 관리
- **게시판** (예정): 커뮤니티 기능

## 🛠️ 기술 스택

| Category | Technologies |
|----------|-------------|
| **Language** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.5.7, Spring Framework 6.x |
| **Database** | PostgreSQL 16 |
| **AI** | Spring AI 1.0.0-M4 (OpenAI Integration) |
| **Build Tool** | Gradle 8.14.3 |
| **Infrastructure** | Docker, Docker Compose |

### 주요 의존성
- Spring Web (RESTful API)
- Spring Data JPA
- Spring AI (OpenAI)
- Spring Actuator (Monitoring)
- PostgreSQL Driver
- Lombok

## 📦 빠른 시작

### 사전 요구사항
- Java 21 이상
- Docker & Docker Compose
- Git

### 설치 및 실행

#### 1. 저장소 클론
```bash
git clone https://github.com/cryschan/BE-Repository.git
cd BE-Repository
```

#### 2. 환경 변수 설정
```bash
cp .env.example .env
```

`.env` 파일을 열어서 다음 값들을 설정하세요:
```env
OPENAI_API_KEY=your-openai-api-key-here
DB_HOST=localhost  # 로컬 실행 시
# DB_HOST=postgres  # Docker 실행 시
```

#### 3-A. 로컬 개발 환경 (PostgreSQL만 Docker 사용)
```bash
# PostgreSQL 시작
docker-compose up -d postgres

# 애플리케이션 실행
./gradlew bootRun
```

#### 3-B. Docker 전체 실행 (프로덕션)
```bash
# .env 파일에서 DB_HOST=postgres로 설정 후
docker-compose up -d
```

#### 4. 접속 확인
```bash
# Health Check
curl http://localhost:8080/actuator/health

# API 테스트
curl http://localhost:8080/api/keyword/fashion
```

## 📡 API 엔드포인트

### 현재 구현된 API

#### 패션 매거진 조회
```http
GET /api/keyword/fashion
```

**응답 예시:**
```json
{
  "keywords": [
    {
      "title": "패션 매거진 제목",
      "url": "https://...",
      "imageUrl": "https://..."
    }
  ]
}
```

### Health Check
```http
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

## 🐳 Docker 사용

### PostgreSQL만 실행
```bash
docker-compose up -d postgres
```

### 전체 서비스 실행
```bash
docker-compose up -d
```

### 로그 확인
```bash
docker-compose logs -f app
```

### 종료
```bash
docker-compose down
```

## 📁 프로젝트 구조

```
BE-Repository/
├── src/main/java/io/github/cryschan/berepository/
│   ├── domain/
│   │   ├── musinsa/    ✅ 구현 완료
│   │   ├── ai/         🚧 구조만 생성
│   │   ├── board/      🚧 구조만 생성
│   │   └── user/       🚧 구조만 생성
│   └── _global/
│       ├── client/     ✅ RestClient 설정
│       ├── config/
│       ├── exception/
│       └── common/
├── docs/
│   └── PROJECT_STRUCTURE.md    📚 상세 문서
├── Dockerfile                  🐳 Multi-stage build
├── docker-compose.yml          🐳 PostgreSQL + App
└── .env.example               🔐 환경 변수 템플릿
```

**상세 문서**: [docs/PROJECT_STRUCTURE.md](./docs/PROJECT_STRUCTURE.md)

## 🔒 보안

- `.env` 파일은 Git에서 제외됩니다 (`.gitignore` 설정됨)
- OpenAI API 키는 반드시 환경 변수로 관리하세요
- Docker 이미지는 Non-root 사용자로 실행됩니다

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 빌드 (테스트 포함)
./gradlew clean build
```

## 📝 개발 현황

### ✅ 완료
- [x] 프로젝트 초기 설정
- [x] Docker 환경 구성 (Multi-stage build)
- [x] Musinsa 도메인 구현
- [x] 무신사 API 연동 테스트

### 🚧 진행 중
- [ ] AI 도메인 구현
- [ ] Board 도메인 구현
- [ ] User 도메인 구현

### ⏳ 예정
- [ ] Global Exception Handler
- [ ] API 표준 응답 DTO
- [ ] Spring Security 인증
- [ ] 테스트 코드 작성
- [ ] API 문서화 (Swagger/Spring REST Docs)

## 🤝 기여

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 개인 학습 목적으로 작성되었습니다.

## 📧 연락처

GitHub: [@cryschan](https://github.com/cryschan)

---

**마지막 업데이트**: 2025-11-18
