# User 예외 처리 리팩토링

**작업 날짜**: 2025-11-23  
**작업자**: cryschan  
**작업 유형**: 리팩토링 (단순화)

---

## 📋 작업 개요

User 도메인의 예외 처리 방식을 개별 예외 클래스에서 정적 팩토리 메서드 패턴으로 리팩토링하여 코드 중복을 제거하고 일관성을 향상시켰습니다.

---

## 🎯 작업 목표

1. ✅ 개별 예외 클래스 제거로 코드 중복 감소
2. ✅ UserException 정적 팩토리 메서드 활용
3. ✅ 일관된 예외 생성 패턴 적용
4. ✅ 유지보수성 향상

---

## 📊 변경 사항

### 1. 예외 구조 변경

#### Before (기존 구조)
```
user/exception/
├── UserException.java (추상 클래스, 정적 팩토리 메서드 포함)
├── DuplicationUserException.java
├── InvalidCredentialsException.java
├── UserNotFoundException.java
└── UnauthorizedException.java
```

#### After (리팩토링 후)
```
user/exception/
├── UserException.java (정적 팩토리 메서드)
└── UnauthorizedException.java
```

**파일 수**: 5개 → 2개 (60% 감소)

---

### 2. UserService.java 변경

#### 회원가입 메서드
```java
// Before
if (userRepository.findByEmail(signupRequest.email()).isPresent()) {
    throw new DuplicationUserException(signupRequest.email());
}

// After
if (userRepository.findByEmail(signupRequest.email()).isPresent()) {
    throw UserException.duplication(signupRequest.email());
}
```

#### 로그인 메서드
```java
// Before - 사용자 조회
User user = userRepository.findByEmail(loginRequest.email())
        .orElseThrow(() -> new InvalidCredentialsException(loginRequest.email()));

// After - 사용자 조회
User user = userRepository.findByEmail(loginRequest.email())
        .orElseThrow(() -> UserException.notFound(loginRequest.email()));

// Before - 비밀번호 검증
if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
    throw new InvalidCredentialsException(loginRequest.email());
}

// After - 비밀번호 검증
if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
    throw UserException.invalidCredentials();
}
```

---

### 3. Import 정리

```java
// 제거된 import
- import io.github.cryschan.berepository.domain.user.exception.DuplicationUserException;
- import io.github.cryschan.berepository.domain.user.exception.InvalidCredentialsException;
- import io.github.cryschan.berepository.domain.user.exception.UserNotFoundException;

// 추가된 import
+ import io.github.cryschan.berepository.domain.user.exception.UserException;
```

---

## 🗑️ 삭제된 파일

1. `DuplicationUserException.java` - 이메일 중복 예외
2. `InvalidCredentialsException.java` - 인증 실패 예외
3. `UserNotFoundException.java` - 사용자 미발견 예외

---

## 📌 UserException 정적 팩토리 메서드

현재 제공되는 정적 팩토리 메서드:

```java
// 사용자 찾기 실패
UserException.notFound(Long userId)
UserException.notFound(String email)
UserException.notFound()

// 이메일 중복
UserException.duplication(String email)

// 인증 실패
UserException.invalidCredentials()

// 인증되지 않은 접근
UserException.unauthorized()
UserException.unauthorized(String message)

// 접근 권한 없음
UserException.accessDenied()
UserException.accessDenied(String resource)
```

---

## ✅ 검증 결과

### 컴파일 테스트
```bash
./gradlew compileJava
```

**결과**: BUILD SUCCESSFUL in 16s

### 영향 범위
- **수정된 파일**: `UserService.java`
- **삭제된 파일**: 3개
- **영향받는 도메인**: User 도메인만

---

## 💡 리팩토링 전략: 옵션 A (단순화)

### 선택한 전략
**옵션 A: 단순화 전략**
- 개별 예외 클래스 삭제
- UserException 정적 팩토리 메서드만 사용

### 대안 (선택하지 않음)
**옵션 B: 하이브리드 전략**
- 개별 예외 클래스 유지
- 정적 팩토리 메서드로 생성
- 특정 예외 타입 catch 가능

### 선택 이유
1. GlobalExceptionHandler에서 일괄 처리 (특정 타입 catch 불필요)
2. 코드 중복 최소화
3. 단순하고 명확한 구조
4. 유지보수 용이

---

## 🔍 예외 처리 흐름

### 비즈니스 예외
```
UserException
  → DomainException
    → BaseException
      → @ExceptionHandler(BaseException.class)
```

### 시스템 에러
```
NullPointerException, IllegalArgumentException 등
  → @ExceptionHandler(Exception.class) (Fallback)
```

---

## 📝 주의사항

### 예외 타입 catch 제한
```java
// ❌ 불가능 (개별 클래스가 없음)
try {
    userService.signup(request);
} catch (DuplicationUserException e) {
    // 특정 처리
}

// ✅ 가능 (ErrorCode로 구분)
try {
    userService.signup(request);
} catch (UserException e) {
    if (e.getErrorCode() == ErrorCode.USER_DUPLICATION) {
        // 중복 처리
    }
}
```

### UnauthorizedException
- 현재 UserService에서 사용하지 않음
- 다른 패키지(security, filter 등)에서 사용 가능성 있음
- 삭제 보류

---

## 📈 개선 효과

### 정량적 효과
- 파일 수 60% 감소 (5개 → 2개)
- 코드 라인 수 약 100라인 감소
- Import 라인 3개 → 1개

### 정성적 효과
- ✅ 코드 중복 제거
- ✅ 일관된 예외 생성 패턴
- ✅ 한 곳에서 모든 예외 관리
- ✅ 새로운 예외 추가 시 UserException에만 메서드 추가
- ✅ IDE 자동완성으로 사용 가능한 예외 쉽게 확인

---

## 🚀 후속 작업 제안

1. [ ] UnauthorizedException 사용처 확인 후 통합 검토
2. [ ] 다른 도메인(Dashboard, FAQ 등)에도 동일 패턴 적용
3. [ ] ErrorCode enum 정리 및 문서화
4. [ ] 예외 처리 가이드 문서 작성

---

## 📚 참고 자료

- `docs/document/EXCEPTION_HANDLING_ARCHITECTURE.md` - 예외 처리 아키텍처
- `src/main/java/io/github/cryschan/berepository/_global/exception/handler/GlobalExceptionHandler.java`
- `src/main/java/io/github/cryschan/berepository/_global/exception/base/ErrorCode.java`

---

## ✏️ 작업 로그

```
[2025-11-23] User 예외 처리 리팩토링 시작
- 현황 조사 완료
- 리팩토링 계획 수립 및 승인
- UserService.java 예외 사용처 변경
- 개별 예외 클래스 3개 삭제
- Import 정리
- 컴파일 검증 완료
```
