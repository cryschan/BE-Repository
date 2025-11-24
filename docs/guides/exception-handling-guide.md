# 예외 처리 사용 가이드

> 새로운 도메인 예외를 쉽고 빠르게 추가하기 위한 실용 가이드

## 📖 목차
- [기본 개념](#기본-개념)
- [빠른 시작](#빠른-시작)
- [단계별 가이드](#단계별-가이드)
- [실제 사용 예제](#실제-사용-예제)
- [체크리스트](#체크리스트)

---

## 기본 개념

### 예외 계층 구조

```
RuntimeException
    └── BaseException (모든 비즈니스 예외의 기본)
            └── DomainException (도메인 예외)
                    ├── UserException
                    ├── BoardException
                    └── FaqException
```

### 핵심 컴포넌트

1. **ErrorCode** (`_global/exception/base/ErrorCode.java`)
   - 모든 에러 코드를 한 곳에서 관리하는 Enum
   - 에러 코드, 메시지, HTTP 상태를 함께 정의

2. **DomainException** 통합 클래스 (예: `UserException`)
   - 도메인별 모든 예외를 대표
   - 정적 팩토리 메서드로 간편하게 예외 생성

3. **GlobalExceptionHandler**
   - 모든 예외를 자동으로 처리
   - 일관된 형식의 ErrorResponse 반환

---

## 빠른 시작

### 새로운 도메인 예외 3단계로 추가하기

#### 1단계: ErrorCode 등록
```java
// _global/exception/base/ErrorCode.java

// Board 도메인 (B로 시작)
BOARD_NOT_FOUND("B001", "게시글을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
BOARD_ACCESS_DENIED("B002", "게시글 접근 권한이 없습니다", HttpStatus.FORBIDDEN),
```

#### 2단계: 통합 예외 클래스 작성
```java
// domain/board/exception/BoardException.java

public class BoardException extends DomainException {

    protected BoardException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected BoardException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    // 정적 팩토리 메서드
    public static BoardException notFound(Long boardId) {
        return new BoardException(
            ErrorCode.BOARD_NOT_FOUND,
            String.format("게시글을 찾을 수 없습니다. ID: %d", boardId)
        );
    }

    public static BoardException accessDenied(Long boardId) {
        return new BoardException(
            ErrorCode.BOARD_ACCESS_DENIED,
            String.format("게시글 %d에 접근 권한이 없습니다", boardId)
        );
    }
}
```

#### 3단계: Service에서 사용
```java
@Service
public class BoardService {

    public BoardResponse findById(Long boardId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> BoardException.notFound(boardId));
        
        return BoardResponse.from(board);
    }
}
```

✅ **끝!** GlobalExceptionHandler가 자동으로 처리합니다.

---

## 단계별 가이드

### Step 1: ErrorCode 추가하기

**위치:** `_global/exception/base/ErrorCode.java`

#### 에러 코드 네이밍 규칙

| Prefix | 도메인 | 범위 | 예시 |
|--------|--------|------|------|
| U | User | U001 ~ U999 | U001: 사용자 미발견 |
| B | Board | B001 ~ B999 | B001: 게시글 미발견 |
| F | FAQ | F001 ~ F999 | F001: FAQ 미발견 |
| C | Common | C001 ~ C999 | C001: 입력값 오류 |

#### 예제

```java
public enum ErrorCode {
    // ==================== Board 도메인 ====================
    BOARD_NOT_FOUND("B001", "게시글을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    BOARD_ACCESS_DENIED("B002", "게시글 접근 권한이 없습니다", HttpStatus.FORBIDDEN),
    BOARD_ALREADY_DELETED("B003", "이미 삭제된 게시글입니다", HttpStatus.GONE),
    
    // ...
}
```

### Step 2: 통합 예외 클래스 작성

**위치:** `domain/{도메인}/exception/{도메인}Exception.java`

#### 기본 템플릿

```java
package io.github.cryschan.berepository.domain.{도메인}.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

public class {도메인}Exception extends DomainException {

    // 생성자 (protected로 제한)
    protected {도메인}Exception(ErrorCode errorCode) {
        super(errorCode);
    }

    protected {도메인}Exception(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    // ==================== 정적 팩토리 메서드 ====================
    
    /**
     * {설명}
     */
    public static {도메인}Exception {메서드명}({파라미터}) {
        return new {도메인}Exception(
            ErrorCode.{ERROR_CODE},
            String.format("{메시지 포맷}", {파라미터})
        );
    }
}
```

#### 실제 예제 (BoardException)

```java
public class BoardException extends DomainException {

    protected BoardException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected BoardException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 게시글을 찾을 수 없을 때
     */
    public static BoardException notFound(Long boardId) {
        return new BoardException(
            ErrorCode.BOARD_NOT_FOUND,
            String.format("게시글을 찾을 수 없습니다. ID: %d", boardId)
        );
    }

    /**
     * 접근 권한이 없을 때
     */
    public static BoardException accessDenied(Long boardId, Long userId) {
        return new BoardException(
            ErrorCode.BOARD_ACCESS_DENIED,
            String.format("사용자 %d는 게시글 %d에 접근할 수 없습니다", userId, boardId)
        );
    }

    /**
     * 이미 삭제된 게시글일 때
     */
    public static BoardException alreadyDeleted() {
        return new BoardException(ErrorCode.BOARD_ALREADY_DELETED);
    }
}
```

### Step 3: Service Layer에서 사용

#### 기본 사용법

```java
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    // 조회
    public BoardResponse findById(Long boardId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> BoardException.notFound(boardId));
        
        return BoardResponse.from(board);
    }

    // 권한 검증
    public void deleteBoard(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> BoardException.notFound(boardId));

        if (!board.isOwner(userId)) {
            throw BoardException.accessDenied(boardId, userId);
        }

        if (board.isDeleted()) {
            throw BoardException.alreadyDeleted();
        }

        boardRepository.delete(board);
    }

    // 비즈니스 로직 검증
    public void updateBoard(Long boardId, UpdateRequest request, Long userId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> BoardException.notFound(boardId));

        if (!board.isOwner(userId)) {
            throw BoardException.accessDenied(boardId, userId);
        }

        board.update(request);
    }
}
```

---

## 실제 사용 예제

### User 도메인 예제

#### UserException.java
```java
public class UserException extends DomainException {

    protected UserException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected UserException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    // ==================== 정적 팩토리 메서드 ====================

    public static UserException notFound(Long userId) {
        return new UserException(
            ErrorCode.USER_NOT_FOUND,
            String.format("사용자를 찾을 수 없습니다. ID: %d", userId)
        );
    }

    public static UserException notFound(String email) {
        return new UserException(
            ErrorCode.USER_NOT_FOUND,
            String.format("사용자를 찾을 수 없습니다. Email: %s", email)
        );
    }

    public static UserException duplication(String email) {
        return new UserException(
            ErrorCode.USER_DUPLICATION,
            String.format("이미 사용 중인 이메일입니다: %s", email)
        );
    }

    public static UserException invalidCredentials() {
        return new UserException(ErrorCode.INVALID_CREDENTIALS);
    }

    public static UserException unauthorized(String message) {
        return new UserException(ErrorCode.UNAUTHORIZED, message);
    }
}
```

#### UserService.java
```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public UserResponse signup(SignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw UserException.duplication(request.email());
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User newUser = User.form(request, encodedPassword);
        User savedUser = userRepository.save(newUser);

        return UserResponse.from(savedUser);
    }

    // 로그인
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> UserException.notFound(request.email()));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw UserException.invalidCredentials();
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUserId());
        return LoginResponse.from(user, accessToken);
    }
}
```

### 응답 예제

#### 성공 시
```json
{
    "userId": 1,
    "email": "user@example.com",
    "name": "홍길동"
}
```

#### 예외 발생 시 (404 - 사용자 미발견)
```json
{
    "message": "사용자를 찾을 수 없습니다. Email: user@example.com",
    "status": 404,
    "code": "U001",
    "timestamp": "2024-11-23T14:30:45.123456",
    "errors": null
}
```

#### 예외 발생 시 (409 - 이메일 중복)
```json
{
    "message": "이미 사용 중인 이메일입니다: user@example.com",
    "status": 409,
    "code": "U002",
    "timestamp": "2024-11-23T14:30:45.123456",
    "errors": null
}
```

---

## 체크리스트

### 새 도메인 예외 추가 시

- [ ] **ErrorCode 등록** (`_global/exception/base/ErrorCode.java`)
  - [ ] 도메인별 prefix 확인 (U, B, F, C 등)
  - [ ] 에러 코드 순번 확인 (001, 002, ...)
  - [ ] 적절한 HTTP 상태 코드 선택
  - [ ] 명확한 기본 메시지 작성

- [ ] **통합 예외 클래스 작성** (`domain/{도메인}/exception/`)
  - [ ] DomainException 상속
  - [ ] protected 생성자 작성
  - [ ] 정적 팩토리 메서드 작성
  - [ ] JavaDoc 주석 추가

- [ ] **Service에서 사용**
  - [ ] 적절한 시점에 예외 발생
  - [ ] 의미있는 파라미터 전달
  - [ ] Optional.orElseThrow() 활용

- [ ] **테스트 작성**
  - [ ] 예외 발생 시나리오 테스트
  - [ ] 에러 메시지 검증
  - [ ] HTTP 상태 코드 검증

### 베스트 프랙티스

#### ✅ 권장사항

1. **정적 팩토리 메서드 사용**
   ```java
   throw UserException.notFound(userId);  // ✅
   ```

2. **구체적인 에러 메시지**
   ```java
   String.format("사용자를 찾을 수 없습니다. ID: %d", userId)  // ✅
   ```

3. **적절한 HTTP 상태 코드**
   ```java
   NOT_FOUND(404), CONFLICT(409), FORBIDDEN(403)  // ✅
   ```

4. **보안 고려**
   ```java
   // 로그인 실패 시 이메일/비밀번호를 구분하지 않음
   throw UserException.invalidCredentials();  // ✅
   ```

#### ❌ 비권장사항

1. **RuntimeException 직접 사용**
   ```java
   throw new RuntimeException("에러");  // ❌
   ```

2. **에러 코드 하드코딩**
   ```java
   ErrorResponse.of("에러", 404, "USER_NOT_FOUND");  // ❌
   ```

3. **민감한 정보 노출**
   ```java
   String.format("비밀번호 %s가 틀렸습니다", password);  // ❌
   ```

4. **너무 일반적인 메시지**
   ```java
   "에러가 발생했습니다"  // ❌
   ```

---

## 참고 문서

- [예외 처리 아키텍처 상세 문서](../architecture/exception-handling.md)
- [Spring Boot Exception Handling](https://spring.io/guides/tutorials/rest/)

---

## 문서 작성 정보

**작성일:** 2024-11-24  
**버전:** 1.0
