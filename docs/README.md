# 📚 Documentation

BE-Repository 프로젝트의 기술 문서 저장소입니다.

## 📂 구조

```
docs/
├── architecture/     # 시스템 아키텍처 및 설계 문서
├── conventions/      # 개발 규칙 및 컨벤션
├── guides/          # 설정 및 사용 가이드
├── domains/         # 도메인별 기능 문서
├── tasks/           # 개발 작업 기록
└── learning/        # 학습 자료 및 참고 문서
```

---

## 📚 Architecture

시스템 전체 아키텍처 및 설계 문서

- [도메인 아키텍처](architecture/domain-architecture.md) - DDD 기반 도메인 설계
- [예외 처리 아키텍처](architecture/exception-handling.md) - 통합 예외 처리 전략
- [ERD](architecture/diagrams/erd/) - 데이터베이스 설계

[더 보기 →](architecture/README.md)

---

## 📋 Conventions

프로젝트 개발 규칙 및 컨벤션

- [Git Flow](conventions/git-flow.md) - Git 브랜치 전략 및 커밋 규칙

[더 보기 →](conventions/README.md)

---

## 🛠️ Guides

개발 환경 설정 및 도구 사용 가이드

### Setup
- [Swagger 설정](guides/setup/swagger-setup.md) - API 문서 자동화

### Best Practices
- [JWT 모범 사례](guides/best-practices/jwt-best-practices.md) - JWT 인증 구현 가이드

[더 보기 →](guides/README.md)

---

## 🏗️ Domains

도메인별 기능 명세 및 구현 문서

### User
사용자 인증, 프로필, 마이페이지 기능

- [Features](domains/user/features/) - 기능 명세서
- [Flows](domains/user/flows/) - 플로우 차트
- [Screens](domains/user/screens/) - 화면 설계

[더 보기 →](domains/user/README.md)

---

## 📝 Tasks

개발 작업 기록 및 이슈 트래킹

### 2025-11
- [User 예외 처리 리팩토링](tasks/2025-11/2025-11-23-user-exception-refactoring.md)
- [User Profile 구현 계획](tasks/2025-11/user-profile-plan.md)

[더 보기 →](tasks/README.md)

---

## 📖 Learning

학습 자료 및 참고 문서

- [예외 처리 학습 가이드](learning/exception-handling-guide.md)

[더 보기 →](learning/README.md)

---

## 🔍 빠른 검색

### 신규 팀원
1. [도메인 아키텍처](architecture/domain-architecture.md)
2. [Git Flow 규칙](conventions/git-flow.md)
3. [Swagger 설정](guides/setup/swagger-setup.md)

### 기능 개발
1. [도메인별 문서](domains/)
2. [예외 처리 가이드](architecture/exception-handling.md)
3. [모범 사례](guides/best-practices/)

### 문제 해결
1. [작업 기록](tasks/)
2. [학습 자료](learning/)

---

## 📝 문서 작성 규칙

### 파일명
- **소문자-하이픈** (kebab-case) 사용
- 예: `user-profile-plan.md`, `jwt-best-practices.md`

### 날짜 형식
- **YYYY-MM-DD** 형식 사용
- 예: `2025-11-23-user-exception-refactoring.md`

### 위치 선택
- **Architecture**: 시스템 전체 설계
- **Conventions**: 팀 규칙
- **Guides**: 설정 및 사용법
- **Domains**: 기능 명세 (도메인별)
- **Tasks**: 작업 기록 (날짜별)
- **Learning**: 학습 및 참고

---

## 📞 문의

문서 관련 문의사항은 프로젝트 팀에 문의해주세요.
