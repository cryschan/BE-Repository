# 예외 처리 사용 가이드

> 처음 사용하는 개발자를 위한 단계별 예외 처리 가이드

## 📖 이 문서에서 배울 것

- 프로젝트에서 예외를 어떻게 처리하는지
- 새로운 예외를 만드는 3가지 간단한 단계
- 실제 상황에서 예외를 사용하는 방법

---

## 🎯 예외 처리란?

프로그램 실행 중 발생하는 문제(예: 사용자를 찾을 수 없음, 이메일 중복)를 체계적으로 관리하는 방법입니다.

### 우리 프로젝트의 예외 처리 방식

```
❌ 이전 방식 (나쁜 예)
throw new RuntimeException("에러 발생!");

✅ 우리 방식 (좋은 예)  
throw UserException.notFound(userId);
```

**장점:**
- 코드가 깔끔하고 읽기 쉬움
- 에러 코드와 메시지가 자동으로 설정됨
- API 응답이 일관된 형식으로 반환됨

---

## 🚀 새로운 예외 만들기 (3단계)

### 상황 예시: 게시글(Board) 도메인에 예외 추가하기

게시글을 찾을 수 없거나, 권한이 없을 때 예외를 발생시켜야 합니다.

---

### 1단계: ErrorCode에 에러 코드 등록

**위치:** `src/main/java/io/github/cryschan/berepository/_global/exception/base/ErrorCode.java`

**파일 열기 → Board 도메인 섹션 찾기 → 코드 추가**

```java
public enum ErrorCode {
    // ... 기존 코드 ...
    
    // ==================== Board 도메인 (B로 시작) ====================
    BOARD_NOT_FOUND("B001", "게시글을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    BOARD_ACCESS_DENIED("B002", "게시글 접근 권한이 없습니다", HttpStatus.FORBIDDEN),
    
    // ... 나머지 코드 ...
}
```

**설명:**
- `"B001"`: 에러 코드 (Board의 B + 순번 001)
- `"게시글을 찾을 수 없습니다"`: 기본 에러 메시지
- `HttpStatus.NOT_FOUND`: HTTP 상태 코드 (404)

**에러 코드 규칙:**

| 도메인 | Prefix | 범위 | 예시 |
|--------|--------|------|------|
| User(사용자) | U | U001~U999 | U001, U002, U003 |
| Board(게시글) | B | B001~B999 | B001, B002, B003 |
| FAQ | F | F001~F999 | F001, F002, F003 |
| 공통 | C | C001~C999 | C001, C002, C003 |

**HTTP 상태 코드 선택:**
- `HttpStatus.NOT_FOUND` (404): 리소스를 찾을 수 없음
- `HttpStatus.FORBIDDEN` (403): 권한 없음
- `HttpStatus.CONFLICT` (409): 중복 (예: 이메일 중복)
- `HttpStatus.UNAUTHORIZED` (401): 인증 실패
- `HttpStatus.BAD_REQUEST` (400): 잘못된 요청

---

### 2단계: 예외 클래스 만들기

**위치:** `src/main/java/io/github/cryschan/berepository/domain/board/exception/BoardException.java`

**파일이 없다면 새로 생성 → 아래 코드 복사-붙여넣기**

```java
package io.github.cryschan.berepository.domain.board.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

public class BoardException extends DomainException {

    // 생성자 (수정하지 않음)
    protected BoardException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected BoardException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    // ==================== 여기부터 추가 ====================
    
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
     * 게시글 접근 권한이 없을 때
     */
    public static BoardException accessDenied(Long boardId, Long userId) {
        return new BoardException(
            ErrorCode.BOARD_ACCESS_DENIED,
            String.format("사용자 %d는 게시글 %d에 접근할 수 없습니다", userId, boardId)
        );
    }
}
```

**설명:**
- `public static BoardException notFound(...)`: 예외를 만드는 메서드
- `String.format(...)`: 에러 메시지에 변수 값 넣기 (예: "게시글을 찾을 수 없습니다. ID: 123")
- 파라미터로 필요한 정보를 받음 (boardId, userId 등)

**패턴:**
```java
public static {도메인}Exception {상황이름}({필요한 파라미터}) {
    return new {도메인}Exception(
        ErrorCode.{에러코드},
        String.format("{메시지}", {파라미터})
    );
}
```

---

### 3단계: Service에서 사용하기

**위치:** `src/main/java/io/github/cryschan/berepository/domain/board/service/BoardService.java`

```java
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    // 예제 1: 게시글 조회
    public BoardResponse findById(Long boardId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> BoardException.notFound(boardId));  // 👈 여기!
        
        return BoardResponse.from(board);
    }

    // 예제 2: 게시글 삭제 (권한 체크)
    public void deleteBoard(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> BoardException.notFound(boardId));

        if (!board.isOwner(userId)) {
            throw BoardException.accessDenied(boardId, userId);  // 👈 여기!
        }

        boardRepository.delete(board);
    }
}
```

**사용 방법:**
1. `Optional.orElseThrow(() -> 예외)`: 값이 없으면 예외 발생
2. `if (조건) throw 예외`: 조건에 맞지 않으면 예외 발생

---

## 💡 실제 사용 예제

### 예제 1: User 도메인

#### 상황: 회원가입 시 이메일 중복 체크

```java
// UserService.java

@Transactional
public UserResponse signup(SignupRequest request) {
    // 이메일 중복 체크
    if (userRepository.findByEmail(request.email()).isPresent()) {
        throw UserException.duplication(request.email());  // 👈 이메일 중복 예외
    }
    
    User savedUser = userRepository.save(User.form(request));
    return UserResponse.from(savedUser);
}
```

#### 상황: 로그인 시 사용자 조회 및 비밀번호 검증

```java
// UserService.java

@Transactional
public LoginResponse login(LoginRequest request) {
    // 1. 사용자 찾기
    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> UserException.notFound(request.email()));  // 👈 사용자 없음
    
    // 2. 비밀번호 검증
    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
        throw UserException.invalidCredentials();  // 👈 비밀번호 틀림
    }
    
    String token = jwtUtil.generateToken(user);
    return LoginResponse.from(user, token);
}
```

---

## 📝 예외 추가 체크리스트

새로운 예외를 추가할 때 이 순서대로 확인하세요!

- [ ] **1단계 완료**: ErrorCode.java에 에러 코드 추가했나요?
  - [ ] 에러 코드 형식: `{도메인PREFIX}{순번}` (예: B001)
  - [ ] HTTP 상태 코드 올바른가요?
  
- [ ] **2단계 완료**: {도메인}Exception.java 파일 만들었나요?
  - [ ] DomainException 상속했나요?
  - [ ] protected 생성자 2개 있나요?
  - [ ] 정적 팩토리 메서드 추가했나요? (예: `notFound`, `accessDenied`)

- [ ] **3단계 완료**: Service에서 예외 사용했나요?
  - [ ] `Optional.orElseThrow()` 사용했나요?
  - [ ] 조건 체크 후 `throw` 했나요?

---

## ⚠️ 주의사항

### 1. RuntimeException 직접 사용 금지

```java
❌ 나쁜 예
throw new RuntimeException("에러 발생");
throw new IllegalArgumentException("잘못된 값");

✅ 좋은 예
throw UserException.notFound(userId);
throw BoardException.accessDenied(boardId, userId);
```

### 2. 에러 메시지는 구체적으로

```java
❌ 나쁜 예
"에러가 발생했습니다"
"찾을 수 없습니다"

✅ 좋은 예
String.format("사용자를 찾을 수 없습니다. ID: %d", userId)
String.format("이미 존재하는 이메일입니다: %s", email)
```

### 3. 보안 주의 - 민감한 정보 노출 금지

```java
❌ 나쁜 예 (비밀번호 노출)
String.format("비밀번호 %s가 틀렸습니다", password)

✅ 좋은 예 (일반적 메시지)
throw UserException.invalidCredentials();  // "인증 정보가 올바르지 않습니다"
```

**이유:** 로그인 실패 시 이메일이 틀렸는지, 비밀번호가 틀렸는지 구분하면 보안 위협이 됩니다.

### 4. 에러 코드 순번 중복 금지

```java
❌ 나쁜 예
BOARD_NOT_FOUND("B001", ..., ...),
BOARD_ACCESS_DENIED("B001", ..., ...),  // B001 중복!

✅ 좋은 예
BOARD_NOT_FOUND("B001", ..., ...),
BOARD_ACCESS_DENIED("B002", ..., ...),  // B002로 순번 증가
```

---

## 💡 유용한 팁

### 팁 1: 기존 예외 클래스 참고하기

처음이라면 이미 만들어진 예외를 보고 따라하세요!

**참고할 파일:**
- `src/.../domain/user/exception/UserException.java` ⭐ 가장 완성도 높음
- `src/.../domain/faqs/exception/FaqException.java`

### 팁 2: IntelliJ 자동완성 활용

1. 예외 클래스에서 `public static` 입력
2. `Ctrl + Space` (자동완성)
3. 메서드 템플릿 선택

### 팁 3: 정적 팩토리 메서드 이름 규칙

| 상황 | 메서드 이름 | 예시 |
|--------|------------|------|
| 찾을 수 없음 | `notFound` | `UserException.notFound(userId)` |
| 중복 | `duplication` | `UserException.duplication(email)` |
| 권한 없음 | `accessDenied` | `BoardException.accessDenied(boardId)` |
| 인증 실패 | `invalidCredentials` | `UserException.invalidCredentials()` |
| 만료됨 | `expired` | `TokenException.expired()` |

### 팁 4: 예외 발생 시 자동으로 응답 변환됨

예외를 던지면 `GlobalExceptionHandler`가 자동으로 처리합니다.

```java
// Service에서
throw UserException.notFound(userId);

// 👇 자동으로 변환됨 👇

// API 응답
{
    "message": "사용자를 찾을 수 없습니다. ID: 123",
    "status": 404,
    "code": "U001",
    "timestamp": "2024-11-24T14:30:45.123456",
    "errors": null
}
```

**따라서:**
- Controller에서 try-catch 불필요
- 에러 응답 직접 만들 필요 없음
- Service에서 예외만 throw하면 끝!

---

## ❓ 자주 묻는 질문

### Q1: ErrorCode와 Exception 파일 위치가 어디인가요?

**A:**
```
src/main/java/io/github/cryschan/berepository/
├── _global/exception/base/
│   └── ErrorCode.java          👈 1단계: 여기에 에러 코드 추가
└── domain/
    ├── user/exception/
    │   └── UserException.java   👈 2단계: 여기 참고
    ├── board/exception/
    │   └── BoardException.java  👈 2단계: 여기에 예외 클래스 생성
    └── faqs/exception/
        └── FaqException.java
```

### Q2: 새 도메인을 추가하는데 Prefix를 뭘로 해야 하나요?

**A:** 도메인 이름의 첫 글자를 대문자로 사용하세요.

**예시:**
- **Comment** 도메인 → **C**001, C002, C003...
  - 하지만 C는 Common이 이미 사용 중!
  - 대안: **CM**001, CM002... (두 글자 사용)
- **Product** 도메인 → **P**001, P002, P003...
- **Order** 도메인 → **O**001, O002, O003...

**중복될 경우:** 두 글자 조합 사용 (예: CM, PR, OR)

### Q3: 파라미터가 없는 예외는 어떻게 만드나요?

**A:** 파라미터 없이 기본 메시지만 사용하면 됩니다.

```java
public static UserException invalidCredentials() {
    return new UserException(ErrorCode.INVALID_CREDENTIALS);
    // 기본 메시지: "인증 정보가 올바르지 않습니다"
}

// 사용
throw UserException.invalidCredentials();
```

### Q4: 여러 개의 정보를 메시지에 넣고 싶어요

**A:** `String.format`에 여러 파라미터를 전달하세요.

```java
public static BoardException accessDenied(Long boardId, Long userId, String reason) {
    return new BoardException(
        ErrorCode.BOARD_ACCESS_DENIED,
        String.format("사용자 %d는 게시글 %d에 접근할 수 없습니다. 사유: %s", 
                      userId, boardId, reason)
    );
}

// 사용
throw BoardException.accessDenied(boardId, userId, "삭제된 게시글");
// 메시지: "사용자 123은 게시글 456에 접근할 수 없습니다. 사유: 삭제된 게시글"
```

### Q5: 에러가 발생하면 자동으로 처리된다는데, 어디서 처리되나요?

**A:** `GlobalExceptionHandler`에서 자동으로 처리됩니다.

**위치:** `src/.../global/exception/handler/GlobalExceptionHandler.java`

이 파일은 수정할 필요가 없습니다! 자동으로 예외를 잡아서 JSON 응답으로 변환해줍니다.

### Q6: 테스트는 어떻게 작성하나요?

**A:** 예외가 제대로 발생하는지 테스트하세요.

```java
@Test
void 사용자를_찾을수없으면_예외발생() {
    // given
    Long userId = 999L;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // when & then
    assertThrows(UserException.class, () -> {
        userService.findById(userId);
    });
}
```

---

## 📚 더 알아보기

### 관련 문서
- [예외 처리 아키텍처 상세 문서](../architecture/exception-handling.md)
- Spring Boot Exception Handling 공식 문서

### 예제 코드 위치
- `src/.../domain/user/exception/UserException.java` - 완성된 예제
- `src/.../domain/user/service/UserService.java` - 실제 사용 예제

---

## 📞 도움이 필요하면?

1. **기존 코드 참고**: UserException.java를 열어보세요
2. **팀원에게 질문**: 예외 처리 경험이 있는 동료에게 물어보세요
3. **이 문서 다시 읽기**: 단계별로 천천히 따라해보세요

---

**작성일:** 2025-11-24
