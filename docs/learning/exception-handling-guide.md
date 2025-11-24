# 예외 처리 구조 학습 가이드

## 📚 개요

이 문서는 BE-Repository 프로젝트의 예외 처리 구조를 효과적으로 학습하기 위한 가이드입니다.
단순히 코드를 복사하는 것이 아니라, **왜 이렇게 설계했는지, 어떤 장점이 있는지** 제대로 이해하는 것을 목표로 합니다.

---

## 🎯 학습 목표

- [ ] 예외 처리 계층 구조 이해
- [ ] ErrorCode 기반 에러 관리 방식 이해
- [ ] GlobalExceptionHandler의 동작 원리 이해
- [ ] 새로운 도메인 예외를 직접 추가할 수 있음
- [ ] 디자인 패턴 인식 및 적용 가능

---

## 📖 학습 로드맵

### 1단계: 계층 구조 이해하기 (Bottom-Up) ⏱️ 2-3일

코드를 읽는 순서가 중요합니다. 아래에서 위로 올라가며 이해하세요.

```
ErrorCode (enum)          ← 1️⃣ 먼저 이해
    ↓
BaseException            ← 2️⃣ 그 다음
    ↓
DomainException          ← 3️⃣ 그 다음
    ↓
UserException            ← 4️⃣ 그 다음
    ↓
GlobalExceptionHandler   ← 5️⃣ 마지막
```

#### 1️⃣ ErrorCode 이해하기

**파일 위치:** `_global/exception/base/ErrorCode.java`

**학습 방법:**
```java
// 코드를 열어서 하나씩 읽어보기
USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND)
```

**질문하며 읽기:**
- ❓ "U001"은 왜 필요할까?
  - 💡 답: 클라이언트가 에러를 구분하기 위해
- ❓ HttpStatus.NOT_FOUND는 몇 번?
  - 💡 답: 404
- ❓ 다른 도메인은 어떤 prefix를 사용할까?
  - 💡 답: B(Board), F(FAQ), C(Common)

**실습:**
```java
// 1. ErrorCode enum 열기
// 2. 각 도메인별로 어떤 에러가 있는지 정리하기
// 3. 새로운 에러 코드 상상해서 추가해보기
BOARD_LIKE_DUPLICATE("B003", "이미 좋아요를 누른 게시글입니다", HttpStatus.CONFLICT)
```

#### 2️⃣ BaseException 이해하기

**파일 위치:** `_global/exception/base/BaseException.java`

**핵심 개념:**
```java
public abstract class BaseException extends RuntimeException {
    private final ErrorCode errorCode;  // ← 이게 핵심!

    protected BaseException(ErrorCode errorCode) { ... }
}
```

**질문하며 읽기:**
- ❓ 왜 `abstract`일까?
  - 💡 답: 직접 생성하지 못하게, 상속만 가능하게
- ❓ 왜 `protected` 생성자일까?
  - 💡 답: 외부에서 직접 생성 못하게, 하위 클래스만 사용
- ❓ `errorCode`를 왜 저장할까?
  - 💡 답: HTTP 상태와 에러 코드를 자동으로 가져오기 위해

**실습:**
```java
// BaseException의 메서드들이 무엇을 반환하는지 확인
public String getCode() { return errorCode.getCode(); }           // "U001"
public HttpStatus getHttpStatus() { return errorCode.getHttpStatus(); }  // NOT_FOUND
public int getHttpStatusCode() { return errorCode.getHttpStatusCode(); } // 404
```

#### 3️⃣ DomainException 이해하기

**파일 위치:** `_global/exception/base/DomainException.java`

**핵심 개념:**
```java
public abstract class DomainException extends BaseException {
    // BaseException의 모든 기능 상속
    // 도메인 예외와 인프라 예외를 구분하기 위한 중간 계층
}
```

**질문하며 읽기:**
- ❓ 왜 BaseException과 UserException 사이에 이게 필요할까?
  - 💡 답: 비즈니스 도메인 예외와 기술적 예외(DB, API 등)를 구분하기 위해
- ❓ 지금은 별로 하는 일이 없는데?
  - 💡 답: 향후 확장성을 위한 설계 (InfrastructureException 추가 시 유용)

#### 4️⃣ UserException 이해하기

**파일 위치:** `domain/user/exception/UserException.java`

**핵심 개념 - 정적 팩토리 메서드:**
```java
// 생성자 대신 의미 있는 이름의 메서드로 예외 생성
public static UserException notFound(Long userId) {
    return new UserException(
        ErrorCode.USER_NOT_FOUND,
        String.format("사용자를 찾을 수 없습니다. ID: %d", userId)
    );
}

// 사용 예시
throw UserException.notFound(999L);  // 명확하고 간결!
```

**비교 학습:**
```java
// ❌ 생성자 직접 호출 (덜 명확함)
throw new UserException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다. ID: 999");

// ✅ 정적 팩토리 메서드 (명확하고 간결함)
throw UserException.notFound(999L);
```

**실습:**
```java
// UserException의 모든 정적 팩토리 메서드 찾아보기
// 1. notFound(Long userId)
// 2. notFound(String email)
// 3. duplication(String email)
// 4. invalidCredentials()
// 5. unauthorized()
// 6. accessDenied()

// 각각 언제 사용하는지 상상해보기
```

#### 5️⃣ GlobalExceptionHandler 이해하기

**파일 위치:** `_global/exception/handler/GlobalExceptionHandler.java`

**핵심 개념 - 통합 예외 처리:**
```java
@ExceptionHandler(BaseException.class)
public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
    return ResponseEntity
        .status(e.getHttpStatus())  // ErrorCode에서 자동으로!
        .body(ErrorResponse.of(
            e.getMessage(),
            e.getHttpStatusCode(),
            e.getCode()
        ));
}
```

**비교 학습 (Before/After):**

**❌ 개선 전 (개별 핸들러):**
```java
@ExceptionHandler(UserNotFoundException.class)
public ErrorResponse handleUserNotFoundException(UserNotFoundException e) {
    return ErrorResponse.of(e.getMessage(), 404, "USER_NOT_FOUND");
}

@ExceptionHandler(DuplicationUserException.class)
public ErrorResponse handleDuplicationUserException(DuplicationUserException e) {
    return ErrorResponse.of(e.getMessage(), 409, "DUPLICATE_EMAIL");
}

// 새 예외 추가할 때마다 핸들러도 추가해야 함 😢
```

**✅ 개선 후 (통합 핸들러):**
```java
@ExceptionHandler(BaseException.class)
public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
    return ResponseEntity
        .status(e.getHttpStatus())  // 자동으로!
        .body(ErrorResponse.of(e.getMessage(), e.getHttpStatusCode(), e.getCode()));
}

// 새 예외 추가해도 자동으로 처리됨 😊
```

---

### 2단계: 흐름 따라가기 (실제 동작 확인) ⏱️ 1-2일

#### 🐛 디버깅으로 학습하기

**준비:**
1. IntelliJ에서 프로젝트 열기
2. 애플리케이션 실행 (`./gradlew bootRun`)
3. Swagger UI 접속 (`http://localhost:8080/swagger-ui.html`)

**실습 1: 브레이크포인트 설정**

```java
// 1단계: UserService.java 열기
public UserResponse findById(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserException.notFound(userId));  // ← 여기에 브레이크포인트
    return UserResponse.from(user);
}

// 2단계: GlobalExceptionHandler.java 열기
@ExceptionHandler(BaseException.class)
public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
    log.error("{}[{}]: {}", ...);  // ← 여기에 브레이크포인트
    return ...;
}
```

**실행 순서:**
1. Swagger에서 존재하지 않는 사용자 조회 (예: ID 999)
2. 첫 번째 브레이크포인트에서 멈춤
3. `F8` (Step Over) 누르면서 진행
4. `UserException.notFound(999L)` 실행
5. 두 번째 브레이크포인트로 이동
6. 변수 창에서 확인:
   - `e.getCode()` → "U001"
   - `e.getHttpStatus()` → NOT_FOUND
   - `e.getMessage()` → "사용자를 찾을 수 없습니다. ID: 999"

**실습 2: 로그 추적**

```bash
# 터미널에서 로그 실시간 확인
./gradlew bootRun | grep -E "UserException|GlobalExceptionHandler|ERROR"
```

**Swagger에서 API 호출:**
```json
GET /api/users/999

# 예상 로그:
# ERROR --- UserNotFoundException[U001]: 사용자를 찾을 수 없습니다. ID: 999
# ERROR --- GlobalExceptionHandler: 에러 응답 전송
```

**예상 응답:**
```json
{
  "message": "사용자를 찾을 수 없습니다. ID: 999",
  "status": 404,
  "code": "U001",
  "timestamp": "2024-11-23T10:30:45",
  "errors": null
}
```

#### 📊 흐름 다이어그램으로 이해하기

```
[Swagger UI]
    │
    │ GET /api/users/999
    ↓
[UserController]
    │
    │ userService.findById(999L)
    ↓
[UserService]
    │
    │ userRepository.findById(999L)
    ↓
[UserRepository]
    │
    │ Optional.empty() 반환
    ↓
[UserService]
    │
    │ throw UserException.notFound(999L)
    ↓
[UserException]
    │
    │ new UserException(ErrorCode.USER_NOT_FOUND, "...")
    ↓
[GlobalExceptionHandler]
    │
    │ @ExceptionHandler(BaseException.class)
    │ handleBaseException(e)
    ↓
[ErrorResponse]
    │
    │ {
    │   "message": "사용자를 찾을 수 없습니다. ID: 999",
    │   "status": 404,
    │   "code": "U001"
    │ }
    ↓
[Swagger UI]
```

---

### 3단계: 확장 실습 (직접 만들어보기) ⏱️ 3-4일

#### 과제 1: Board 도메인 예외 만들기

**Step 1: ErrorCode에 Board 예외 추가**

```java
// _global/exception/base/ErrorCode.java
public enum ErrorCode {
    // ... 기존 코드 ...

    // ==================== Board 도메인 (B로 시작) ====================
    BOARD_NOT_FOUND("B001", "게시글을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    BOARD_ACCESS_DENIED("B002", "게시글에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),
    BOARD_DELETE_FORBIDDEN("B003", "게시글은 작성자만 삭제할 수 있습니다", HttpStatus.FORBIDDEN),
}
```

**Step 2: BoardException 클래스 생성**

```java
// domain/board/exception/BoardException.java
package io.github.cryschan.berepository.domain.board.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

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

    public static BoardException accessDenied(Long boardId, Long userId) {
        return new BoardException(
            ErrorCode.BOARD_ACCESS_DENIED,
            String.format("게시글 %d에 대한 권한이 없습니다. User ID: %d", boardId, userId)
        );
    }

    public static BoardException deleteForbidden(Long boardId) {
        return new BoardException(
            ErrorCode.BOARD_DELETE_FORBIDDEN,
            String.format("게시글 %d는 작성자만 삭제할 수 있습니다", boardId)
        );
    }
}
```

**Step 3: BoardService에서 사용**

```java
// domain/board/service/BoardService.java
@Service
public class BoardService {

    public BoardResponse findById(Long boardId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> BoardException.notFound(boardId));
        return BoardResponse.from(board);
    }

    public void delete(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> BoardException.notFound(boardId));

        if (!board.isOwner(userId)) {
            throw BoardException.deleteForbidden(boardId);
        }

        boardRepository.delete(board);
    }
}
```

**Step 4: API 호출해서 확인**

```bash
# Swagger에서 테스트
GET /api/boards/999

# 예상 응답:
{
  "message": "게시글을 찾을 수 없습니다. ID: 999",
  "status": 404,
  "code": "B001",
  "timestamp": "2024-11-23T10:30:45",
  "errors": null
}
```

**✅ 확인 사항:**
- [ ] GlobalExceptionHandler에 별도 코드 추가 안 해도 작동하는가?
- [ ] 로그에 BoardException이 찍히는가?
- [ ] HTTP 상태 코드가 올바른가?
- [ ] 에러 코드 "B001"이 응답에 포함되는가?

#### 과제 2: FAQ 도메인 예외 만들기

**스스로 해보기:**
1. ErrorCode에 FAQ 예외 추가 (F001, F002, ...)
2. FaqException 클래스 생성
3. FaqService에서 사용
4. API 테스트

#### 과제 3: 커스텀 에러 시나리오 추가

**상황: 게시글 좋아요 기능**
```java
// 이미 좋아요를 누른 게시글에 다시 좋아요를 누르려고 할 때

// 1. ErrorCode 추가
BOARD_LIKE_DUPLICATE("B004", "이미 좋아요를 누른 게시글입니다", HttpStatus.CONFLICT),

// 2. BoardException에 정적 팩토리 메서드 추가
public static BoardException likeDuplicate(Long boardId) {
    return new BoardException(
        ErrorCode.BOARD_LIKE_DUPLICATE,
        String.format("게시글 %d에 이미 좋아요를 눌렀습니다", boardId)
    );
}

// 3. BoardService에서 사용
public void likeBoard(Long boardId, Long userId) {
    if (likeRepository.existsByBoardIdAndUserId(boardId, userId)) {
        throw BoardException.likeDuplicate(boardId);
    }
    // 좋아요 처리...
}
```

---

### 4단계: 테스트 작성 (제대로 이해했는지 확인) ⏱️ 2-3일

#### 단위 테스트 작성하기

```java
// test/.../domain/user/exception/UserExceptionTest.java
package io.github.cryschan.berepository.domain.user.exception;

import io.github.cryschan.berepository._global.exception.base.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class UserExceptionTest {

    @Test
    @DisplayName("notFound(Long) - 사용자 ID로 예외 생성")
    void notFound_with_userId() {
        // Given
        Long userId = 999L;

        // When
        UserException exception = UserException.notFound(userId);

        // Then
        assertThat(exception.getCode()).isEqualTo("U001");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getHttpStatusCode()).isEqualTo(404);
        assertThat(exception.getMessage()).contains("999");
        assertThat(exception.getMessage()).contains("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("notFound(String) - 이메일로 예외 생성")
    void notFound_with_email() {
        // Given
        String email = "test@example.com";

        // When
        UserException exception = UserException.notFound(email);

        // Then
        assertThat(exception.getCode()).isEqualTo("U001");
        assertThat(exception.getMessage()).contains("test@example.com");
    }

    @Test
    @DisplayName("duplication - 이메일 중복 예외")
    void duplication() {
        // Given
        String email = "duplicate@example.com";

        // When
        UserException exception = UserException.duplication(email);

        // Then
        assertThat(exception.getCode()).isEqualTo("U002");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getHttpStatusCode()).isEqualTo(409);
        assertThat(exception.getMessage()).contains("duplicate@example.com");
    }

    @Test
    @DisplayName("invalidCredentials - 로그인 실패 예외")
    void invalidCredentials() {
        // When
        UserException exception = UserException.invalidCredentials();

        // Then
        assertThat(exception.getCode()).isEqualTo("U003");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getHttpStatusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("unauthorized - 인증 필요 예외")
    void unauthorized() {
        // When
        UserException exception = UserException.unauthorized();

        // Then
        assertThat(exception.getCode()).isEqualTo("U004");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
```

#### 통합 테스트 작성하기

```java
// test/.../domain/user/controller/UserControllerTest.java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 404 에러")
    void getUserById_NotFound() throws Exception {
        // Given
        Long nonExistentUserId = 999L;

        // When & Then
        mockMvc.perform(get("/api/users/{id}", nonExistentUserId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("U001"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").exists())
            .andDo(print());
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 409 에러")
    void signup_DuplicateEmail() throws Exception {
        // Given
        String duplicateEmail = "existing@example.com";
        // 먼저 사용자 생성...

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + duplicateEmail + "\",\"password\":\"test123\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("U002"))
            .andExpect(jsonPath("$.status").value(409))
            .andDo(print());
    }
}
```

**실습 과제:**
- [ ] UserExceptionTest 작성
- [ ] BoardExceptionTest 작성
- [ ] FaqExceptionTest 작성
- [ ] Controller 통합 테스트 작성

---

### 5단계: 패턴 인식 (디자인 패턴 학습) ⏱️ 2-3일

이 코드에서 사용된 **디자인 패턴**들을 인식하고 이해합니다.

#### ① 정적 팩토리 메서드 패턴 (Static Factory Method Pattern)

**개념:**
생성자 대신 정적 메서드로 인스턴스를 생성하는 패턴

**장점:**
- 메서드 이름으로 의도를 명확히 전달
- 매번 새 객체를 만들지 않아도 됨
- 하위 타입 객체 반환 가능

**적용 예시:**
```java
// ❌ 생성자 직접 호출 (의도가 불명확)
throw new UserException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다. ID: 999");

// ✅ 정적 팩토리 메서드 (의도가 명확)
throw UserException.notFound(999L);
throw UserException.duplication("test@email.com");
throw UserException.invalidCredentials();
```

**실습:**
```java
// 다른 곳에서도 정적 팩토리 메서드 찾아보기
LocalDate.of(2024, 11, 23);        // new LocalDate()가 아님
Optional.of(value);                // new Optional()가 아님
Collections.emptyList();           // new ArrayList()가 아님
```

#### ② 템플릿 메서드 패턴 (Template Method Pattern)

**개념:**
상위 클래스에서 뼈대를 정의하고, 하위 클래스에서 구체화

**적용 예시:**
```java
// BaseException: 뼈대 정의
public abstract class BaseException extends RuntimeException {
    private final ErrorCode errorCode;  // 공통 필드
    public String getCode() { ... }      // 공통 메서드
}

// DomainException: 도메인용으로 특화
public abstract class DomainException extends BaseException {
    // BaseException의 기능 상속
}

// UserException: User 도메인용으로 구체화
public class UserException extends DomainException {
    public static UserException notFound(Long userId) { ... }  // 구체적 구현
}
```

#### ③ 전략 패턴 (Strategy Pattern)

**개념:**
알고리즘을 캡슐화하고 교체 가능하게 만드는 패턴

**적용 예시:**
```java
// ErrorCode enum이 에러 처리 전략을 캡슐화
public enum ErrorCode {
    USER_NOT_FOUND("U001", "...", HttpStatus.NOT_FOUND),      // 전략 1
    USER_DUPLICATION("U002", "...", HttpStatus.CONFLICT),      // 전략 2
    INVALID_CREDENTIALS("U003", "...", HttpStatus.UNAUTHORIZED) // 전략 3
}

// 사용 시점에 전략 선택
throw new UserException(ErrorCode.USER_NOT_FOUND);  // 전략 1 선택
throw new UserException(ErrorCode.USER_DUPLICATION); // 전략 2 선택
```

#### ④ 단일 책임 원칙 (Single Responsibility Principle)

**적용 예시:**
- **ErrorCode**: 에러 코드 정의만 담당
- **BaseException**: 예외 기본 기능만 담당
- **UserException**: User 도메인 예외만 담당
- **GlobalExceptionHandler**: 예외를 HTTP 응답으로 변환만 담당

#### ⑤ 개방-폐쇄 원칙 (Open-Closed Principle)

**개념:**
확장에는 열려있고, 수정에는 닫혀있어야 함

**적용 예시:**
```java
// ✅ 새 예외 추가 시 (확장에 열려있음)
// 1. ErrorCode에 새 코드 추가
BOARD_NOT_FOUND("B001", "...", HttpStatus.NOT_FOUND)

// 2. BoardException 생성
public class BoardException extends DomainException { ... }

// 3. GlobalExceptionHandler는 수정 불필요! (수정에 닫혀있음)
// 자동으로 BaseException 핸들러가 처리
```

---

## 📚 학습 체크리스트

### 기초 이해
- [ ] ErrorCode enum의 구조를 설명할 수 있다
- [ ] BaseException의 역할을 설명할 수 있다
- [ ] DomainException vs BaseException 차이를 설명할 수 있다
- [ ] 정적 팩토리 메서드의 장점 3가지를 말할 수 있다
- [ ] GlobalExceptionHandler가 어떻게 모든 예외를 처리하는지 설명할 수 있다

### 실습 완료
- [ ] 디버거로 예외 발생부터 응답까지 흐름을 추적했다
- [ ] 로그를 보고 어떤 순서로 실행되는지 확인했다
- [ ] Board 도메인 예외를 직접 추가했다
- [ ] FAQ 도메인 예외를 직접 추가했다
- [ ] 단위 테스트를 작성했다
- [ ] 통합 테스트를 작성했다

### 심화 이해
- [ ] 왜 계층 구조가 필요한지 3가지 이유를 설명할 수 있다
- [ ] 새 예외 추가 시 어디를 수정해야 하는지 순서대로 말할 수 있다
- [ ] 다른 프로젝트에 이 구조를 적용할 수 있다
- [ ] 디자인 패턴 3가지 이상을 코드에서 찾을 수 있다
- [ ] 이 구조의 장단점을 설명할 수 있다

---

## 🎯 3주 학습 계획

### 1주차: 구조 이해 및 흐름 파악

| 요일 | 학습 내용 | 시간 | 완료 |
|------|-----------|------|------|
| 월 | ErrorCode, BaseException 읽기 | 1-2h | [ ] |
| 화 | DomainException, UserException 읽기 | 1-2h | [ ] |
| 수 | GlobalExceptionHandler 읽기 | 1-2h | [ ] |
| 목 | 디버거로 흐름 추적 실습 | 2-3h | [ ] |
| 금 | 로그 분석 및 복습 | 1-2h | [ ] |
| 주말 | 1주차 정리 및 블로그 작성 | 2-3h | [ ] |

### 2주차: 실습 및 확장

| 요일 | 학습 내용 | 시간 | 완료 |
|------|-----------|------|------|
| 월 | Board 도메인 예외 만들기 | 2-3h | [ ] |
| 화 | FAQ 도메인 예외 만들기 | 2-3h | [ ] |
| 수 | 커스텀 에러 시나리오 추가 | 2-3h | [ ] |
| 목 | 단위 테스트 작성 | 2-3h | [ ] |
| 금 | 통합 테스트 작성 | 2-3h | [ ] |
| 주말 | 2주차 정리 및 블로그 작성 | 2-3h | [ ] |

### 3주차: 심화 및 응용

| 요일 | 학습 내용 | 시간 | 완료 |
|------|-----------|------|------|
| 월 | 디자인 패턴 학습 | 2-3h | [ ] |
| 화 | 다른 프로젝트에 적용해보기 | 3-4h | [ ] |
| 수 | Spring 공식 문서 읽기 | 1-2h | [ ] |
| 목 | 코드 리뷰 (스스로) | 2-3h | [ ] |
| 금 | 개선점 찾기 및 리팩토링 | 2-3h | [ ] |
| 주말 | 최종 정리 및 발표 자료 만들기 | 3-4h | [ ] |

---

## 💡 학습 팁

### 1. 질문하며 읽기
코드를 읽을 때 항상 "왜?"를 물어보세요.
- 왜 이렇게 설계했을까?
- 다른 방법은 없을까?
- 장단점은 무엇일까?

### 2. 손으로 직접 그리기
계층 구조, 흐름도를 손으로 그려보세요.
- 머리로만 이해하는 것과 큰 차이가 있습니다
- 그릴 수 없다면 제대로 이해하지 못한 것

### 3. 실제로 써보기
읽기만 하지 말고 직접 코드를 작성하세요.
- Board, FAQ 도메인 예외 추가
- 새로운 에러 시나리오 상상하기
- 테스트 코드 작성

### 4. 비교하며 학습하기
개선 전/후를 비교하며 장점을 체감하세요.
- 개별 핸들러 vs 통합 핸들러
- 생성자 vs 정적 팩토리 메서드
- 하드코딩 vs ErrorCode enum

### 5. 블로그에 정리하기
배운 내용을 블로그에 정리하세요.
- 다른 사람에게 설명할 수 있어야 진짜 이해한 것
- 나중에 참고 자료로 활용 가능

### 6. 실무 관점에서 생각하기
- 팀 프로젝트에서 이걸 어떻게 활용할까?
- 신입 개발자가 이 코드를 봤을 때 이해하기 쉬울까?
- 6개월 후 내가 봤을 때 이해할 수 있을까?

---

## 📖 참고 자료

### 공식 문서
- [Spring Boot Exception Handling](https://spring.io/guides/tutorials/rest/)
- [Spring @ExceptionHandler](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/ExceptionHandler.html)
- [HTTP Status Codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)

### 디자인 패턴
- [정적 팩토리 메서드](https://johngrib.github.io/wiki/static-factory-method-pattern/)
- [템플릿 메서드 패턴](https://refactoring.guru/design-patterns/template-method)
- [전략 패턴](https://refactoring.guru/design-patterns/strategy)

### 추천 도서
- 『Effective Java』 - Joshua Bloch (정적 팩토리 메서드)
- 『Clean Code』 - Robert C. Martin (예외 처리)
- 『Head First Design Patterns』 - Eric Freeman (디자인 패턴 입문)

### 프로젝트 문서
- [예외 처리 아키텍처](../../document/EXCEPTION_HANDLING_ARCHITECTURE.md)
- [도메인 아키텍처](../../document/DOMAIN_ARCHITECTURE.md)

---

## 🤔 자주 묻는 질문 (FAQ)

### Q1. 왜 이렇게 복잡하게 만들었나요? 그냥 RuntimeException 쓰면 안 되나요?

**A:** 간단한 프로젝트라면 RuntimeException으로 충분할 수 있습니다. 하지만:

```java
// ❌ RuntimeException 직접 사용
throw new RuntimeException("사용자를 찾을 수 없습니다");

// 문제점:
// 1. HTTP 상태 코드를 어떻게 결정? (항상 500?)
// 2. 에러 코드는? (클라이언트가 구분 불가)
// 3. 새 예외 추가 시 GlobalExceptionHandler도 수정
// 4. 팀원들이 일관성 없이 예외 던짐

// ✅ 구조화된 예외 사용
throw UserException.notFound(userId);

// 장점:
// 1. HTTP 상태 자동 결정 (404)
// 2. 에러 코드 자동 ("U001")
// 3. GlobalExceptionHandler 수정 불필요
// 4. 팀원들이 일관된 방식으로 예외 던짐
```

### Q2. BaseException과 DomainException 둘 다 필요한가요?

**A:** 현재는 비슷해 보이지만, 확장성을 위해 분리했습니다.

```java
// 향후 확장 시:
BaseException
├── DomainException (비즈니스 로직 예외)
│   ├── UserException
│   └── BoardException
└── InfrastructureException (기술적 예외)
    ├── DatabaseException
    ├── ExternalApiException
    └── CacheException

// 각각 다른 방식으로 처리 가능
@ExceptionHandler(DomainException.class)  // 사용자에게 상세 메시지
@ExceptionHandler(InfrastructureException.class)  // 일반 메시지만
```

### Q3. 정적 팩토리 메서드 vs 개별 예외 클래스, 뭘 써야 하나요?

**A:** 둘 다 사용 가능합니다. 취향과 상황에 따라 선택하세요.

```java
// 방법 1: 정적 팩토리 메서드 (권장)
throw UserException.notFound(userId);
// 장점: 간결, 중복 코드 감소
// 단점: IDE에서 "Go to Definition" 시 UserException으로 이동

// 방법 2: 개별 예외 클래스
throw new UserNotFoundException(userId);
// 장점: 타입이 명확, IDE에서 직접 이동
// 단점: 클래스 파일 많아짐
```

### Q4. ErrorCode를 데이터베이스에 저장하는 게 좋을까요?

**A:** 일반적으로 enum으로 충분합니다.

```java
// ✅ Enum 사용 (권장)
// - 컴파일 타임에 오류 확인
// - 성능 좋음
// - 코드 변경 시 배포만 하면 됨

// ❌ DB 저장 (특수한 경우만)
// - 런타임에 에러 코드 변경 가능해야 할 때
// - 다국어 지원을 DB에서 관리할 때
// - 하지만 대부분의 프로젝트에는 과도함
```

---

## ✍️ 학습 일지 템플릿

```markdown
# 예외 처리 학습 일지 - [날짜]

## 오늘 학습한 내용
-

## 새롭게 알게 된 점
-

## 이해가 안 되는 부분
-

## 실습한 내용
-

## 내일 학습할 내용
-

## 메모
-
```

---

## 🎓 마치며

이 학습 가이드를 끝까지 완료하면:
- ✅ 체계적인 예외 처리 구조를 설계할 수 있습니다
- ✅ Spring Boot 베스트 프랙티스를 이해합니다
- ✅ 디자인 패턴을 실무에 적용할 수 있습니다
- ✅ 유지보수하기 좋은 코드를 작성할 수 있습니다

**중요한 것은 단순히 코드를 복사하는 것이 아니라,
"왜 이렇게 설계했는지" 이해하는 것입니다!**

화이팅! 💪

---

**문서 이력**

| 날짜 | 작성자 | 내용 |
|------|--------|------|
| 2024-11-23 | cryschan | 초기 문서 작성 |
