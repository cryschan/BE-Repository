# Git-flow 컨벤션 가이드

## 📌 개요

Git-flow는 협업 프로젝트에서 브랜치를 체계적으로 관리하고 배포 프로세스를 표준화하기 위한 브랜치 전략입니다. 본 문서는 우리 팀의 Git-flow 사용 규칙과 컨벤션을 정의합니다.

## 🎯 목적

- **브랜치 충돌 최소화**: 기능 개발, 배포, 긴급 수정 과정 분리
- **안정적인 배포 보장**: 체계적인 브랜치 관리를 통한 배포 안정성 확보
- **효율적인 협업**: 명확한 규칙을 통한 팀 협업 효율성 향상

## 🌳 브랜치 구조

### 메인 브랜치 (항상 유지)

| 브랜치 | 역할 | 설명 |
| --- | --- | --- |
| **main** | 운영(배포) 브랜치 | 프로덕션 환경에 배포되는 코드, 항상 안정적 상태 유지 |
| **develop** | 개발 통합 브랜치 | 다음 릴리스를 위한 개발 코드 통합, 기본 브랜치(default branch) |

### 보조 브랜치 (일시적 유지)

| 브랜치 | 역할 | 생성 원점 | 병합 대상 |
| --- | --- | --- | --- |
| **feature/*** | 기능 개발 | develop | develop |
| **release/*** | 배포 준비 | develop | main, develop |
| **hotfix/*** | 긴급 버그 수정 | main | main, develop |

## 📝 네이밍 규칙

### 브랜치 네이밍

```bash
feature/기능명     # 예: feature/login-api, feature/user-profile
release/버전      # 예: release/1.0.0, release/2.1.0
hotfix/이슈명     # 예: hotfix/payment-error, hotfix/critical-bug
```

### 커밋 메시지 컨벤션

```bash
<type>: <subject>

<body> (선택사항)

<footer> (선택사항)
```

#### Type 종류

| Type | 설명 | 예시 |
| --- | --- | --- |
| **feat** | 새로운 기능 추가 | `feat: 사용자 로그인 API 구현` |
| **fix** | 버그 수정 | `fix: 주문 처리 오류 수정` |
| **refactor** | 코드 리팩토링 | `refactor: 사용자 서비스 구조 개선` |
| **docs** | 문서 수정 | `docs: README 설치 가이드 추가` |
| **style** | 코드 포맷팅, 세미콜론 누락 등 | `style: 코드 포맷 정리` |
| **test** | 테스트 코드 추가/수정 | `test: 사용자 서비스 단위 테스트 추가` |
| **chore** | 빌드, 패키지 매니저 설정 등 | `chore: gradle 의존성 업데이트` |
| **perf** | 성능 개선 | `perf: 쿼리 최적화로 응답 속도 개선` |

#### 커밋 메시지 작성 예시

```bash
feat: 사용자 인증 JWT 토큰 구현

- Access Token과 Refresh Token 분리
- Token 만료 시간 설정 (Access: 1시간, Refresh: 14일)
- SecurityConfig에 JWT 필터 추가

Resolves: #123
```

## 🔀 Merge 규칙

### Pull Request 규칙

1. **제목 작성**: 커밋 메시지 컨벤션과 동일하게 작성
   - 예: `feat: 사용자 프로필 조회 API 추가`

2. **코드 리뷰 필수**
   - `develop` 브랜치 병합: **최소 2명의 승인** 필요
   - `main` 브랜치 병합: **최소 1명의 승인** 필요

3. **PR 템플릿 활용**
   - 작업 내용 상세 설명
   - 테스트 결과 첨부
   - 관련 이슈 번호 명시

### Merge 전략

| 상황 | 병합 방법 | 이유 |
| --- | --- | --- |
| `feature` → `develop` | **Squash Merge** | 기능별 하나의 커밋으로 정리 |
| `release` → `main` | **Merge Commit** | 릴리스 이력 보존 |
| `hotfix` → `main` | **Merge Commit** | 긴급 수정 이력 명확히 보존 |

## 🚀 워크플로우

### 1. 기능 개발 (Feature)

```bash
# 1. develop에서 feature 브랜치 생성
git checkout develop
git pull origin develop
git checkout -b feature/기능명

# 2. 기능 개발 및 커밋
git add .
git commit -m "feat: 기능 설명"

# 3. develop 최신화 후 push
git checkout develop
git pull origin develop
git checkout feature/기능명
git merge develop
git push origin feature/기능명

# 4. PR 생성 및 리뷰 요청
```

### 2. 배포 준비 (Release)

```bash
# 1. develop에서 release 브랜치 생성
git checkout develop
git pull origin develop
git checkout -b release/1.0.0

# 2. 버전 정보 업데이트, 버그 수정
git commit -m "chore: 버전 1.0.0 업데이트"

# 3. main과 develop에 병합
git checkout main
git merge --no-ff release/1.0.0
git tag -a v1.0.0 -m "Version 1.0.0"
git push origin main --tags

git checkout develop
git merge --no-ff release/1.0.0
git push origin develop

# 4. release 브랜치 삭제
git branch -d release/1.0.0
```

### 3. 긴급 수정 (Hotfix)

```bash
# 1. main에서 hotfix 브랜치 생성
git checkout main
git pull origin main
git checkout -b hotfix/긴급수정

# 2. 버그 수정 및 커밋
git commit -m "fix: 긴급 버그 수정"

# 3. main과 develop에 병합
git checkout main
git merge --no-ff hotfix/긴급수정
git tag -a v1.0.1 -m "Hotfix version 1.0.1"
git push origin main --tags

git checkout develop
git merge --no-ff hotfix/긴급수정
git push origin develop

# 4. hotfix 브랜치 삭제
git branch -d hotfix/긴급수정
```

## 🛡️ GitHub 브랜치 보호 정책

### main 브랜치 보호

- **Ruleset Name**: `main-protection-v1`
- **요구사항**:
  - Pull Request 필수
  - 최소 1명의 승인 필요
  - 새 커밋 시 기존 승인 무효화
  - 모든 코멘트 해결 후 병합
  - Force push 금지
  - 브랜치 삭제 금지
  - Merge Commit만 허용

### develop 브랜치 보호 (Default Branch)

- **Ruleset Name**: `develop-protection-v1`
- **요구사항**:
  - Pull Request 필수
  - 최소 2명의 승인 필요
  - 새 커밋 시 기존 승인 무효화
  - 모든 코멘트 해결 후 병합
  - Force push 금지
  - 브랜치 삭제 금지
  - Squash Merge, Merge Commit 허용

## ✅ 체크리스트

### PR 생성 전

- [ ] 최신 develop 브랜치와 병합 완료
- [ ] 모든 테스트 통과
- [ ] 코드 포맷팅 확인
- [ ] 불필요한 console.log, 주석 제거
- [ ] 커밋 메시지 컨벤션 준수

### 코드 리뷰 시

- [ ] 비즈니스 로직 검증
- [ ] 보안 취약점 확인
- [ ] 성능 이슈 검토
- [ ] 코드 가독성 및 유지보수성
- [ ] 테스트 커버리지 확인

## 📚 참고 자료

- [우린 Git-flow를 사용하고 있어요 | 우아한형제들 기술블로그](https://techblog.woowahan.com/2553/)
- [Git-flow Cheatsheet](https://danielkummer.github.io/git-flow-cheatsheet/)
- [Conventional Commits](https://www.conventionalcommits.org/)

---

**문서 버전**: v1.0.0  
**최종 수정일**: 2025-11-21  
**작성자**: Development Team