# 예외 처리 아키텍처 (Exception Handling Architecture)

## 📋 목차
- [개요](#개요)
- [리팩토링 배경](#리팩토링-배경)
- [예외 계층 구조](#예외-계층-구조)
- [ErrorCode 체계](#errorcode-체계)
- [코드 흐름](#코드-흐름)
- [주요 컴포넌트](#주요-컴포넌트)
- [사용 가이드](#사용-가이드)
- [응답 형식](#응답-형식)
- [확장 가이드](#확장-가이드)

---

## 개요

본 프로젝트는 **체계적이고 확장 가능한 예외 처리 구조**를 구현하기 위해 Spring Boot 3.x 베스트 프랙티스를 적용했습니다. 모든 비즈니스 예외는 계층적 구조로 관리되며, 일관된 에러 응답을 제공합니다.

### 주요 특징
- ✅ 계층적 예외 구조 (BaseException → DomainException → 도메인별 예외)
- ✅ 체계적인 에러 코드 관리 (ErrorCode enum)
- ✅ 일관된 에러 응답 형식 (ErrorResponse)
- ✅ 중앙집중식 예외 처리 (GlobalExceptionHandler)
- ✅ 도메인별 예외 그룹화 및 정적 팩토리 메서드 제공

---

## 리팩토링 배경

### 리팩토링 이전 구조의 문제점

```java
// Before: 평면적 구조
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

@ExceptionHandler(UserNotFoundException.class)
public ErrorResponse handleUserNotFoundException(UserNotFoundException e) {
    return ErrorResponse.of(e.getMessage(), 404, "USER_NOT_FOUND");
}
```

**문제점:**
1. 모든 예외가 RuntimeException을 직접 상속 (평면적 구조)
2. 에러 코드가 문자열로 하드코딩 (일관성 부족)
3. 예외마다 별도 핸들러 필요 (중복 코드)
4. HTTP 상태 코드가 핸들러에 하드코딩 (변경 어려움)
5. 도메인별 예외 그룹화 없음 (관리 어려움)

### 리팩토링 후 개선 사항

```java
// After: 계층적 구조
public class UserNotFoundException extends UserException {
    public UserNotFoundException(Long userId) {
        super(ErrorCode.USER_NOT_FOUND,
              String.format("사용자를 찾을 수 없습니다. ID: %d", userId));
    }
}

@ExceptionHandler(BaseException.class)
public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
    return ResponseEntity
        .status(e.getHttpStatus())
        .body(ErrorResponse.of(e.getMessage(), e.getHttpStatusCode(), e.getCode()));
}
```

**개선점:**
1. 명확한 예외 계층 구조
2. 중앙집중식 에러 코드 관리 (ErrorCode enum)
3. 단일 핸들러로 모든 비즈니스 예외 처리
4. ErrorCode에서 HTTP 상태 코드 관리
5. 도메인별 예외 클래스로 그룹화

---

## 예외 계층 구조

### 전체 구조 다이어그램

```
java.lang.RuntimeException
        │
        ├── BaseException (추상 클래스)
        │   │
        │   ├── DomainException (추상 클래스) - 비즈니스 도메인 예외
        │   │   │
        │   │   ├── UserException - User 도메인 통합 예외
        │   │   │   ├── UserNotFoundException
        │   │   │   ├── DuplicationUserException
        │   │   │   ├── InvalidCredentialsException
        │   │   │   └── UnauthorizedException
        │   │   │
        │   │   ├── BoardException - Board 도메인 (향후 추가)
        │   │   │   ├── BoardNotFoundException
        │   │   │   └── BoardAccessDeniedException
        │   │   │
        │   │   └── FaqException - FAQ 도메인 (향후 추가)
        │   │       ├── FaqNotFoundException
        │   │       └── FaqAccessDeniedException
        │   │
        │   └── InfrastructureException (향후 확장)
        │       ├── DatabaseException
        │       ├── ExternalApiException
        │       └── MessageQueueException
        │
        └── Spring Framework Exceptions
            ├── MethodArgumentNotValidException (Validation)
            └── Exception (Fallback)
```

### 계층별 역할

#### 1. BaseException (기본 추상 클래스)
```java
public abstract class BaseException extends RuntimeException {
    private final ErrorCode errorCode;

    // ErrorCode 기반 예외 관리
    // HTTP 상태 코드 제공
    // 에러 코드 제공
}
```

**역할:**
- 모든 애플리케이션 예외의 최상위 부모
- ErrorCode와 연동하여 일관된 에러 정보 제공
- HTTP 상태 코드와 에러 코드를 중앙에서 관리

#### 2. DomainException (도메인 추상 클래스)
```java
public abstract class DomainException extends BaseException {
    // BaseException의 생성자를 그대로 사용
}
```

**역할:**
- 비즈니스 도메인 관련 예외와 기술적 예외 분리
- 도메인 예외에 대한 특별한 처리 전략 적용 가능
- 향후 InfrastructureException과 차별화된 처리

#### 3. UserException (도메인별 통합 예외)
```java
public class UserException extends DomainException {
    // 정적 팩토리 메서드로 다양한 예외 생성
    public static UserException notFound(Long userId) { ... }
    public static UserException duplication(String email) { ... }
    public static UserException unauthorized() { ... }
}
```

**역할:**
- User 도메인의 모든 예외를 대표
- 정적 팩토리 메서드로 간편한 예외 생성
- 기존 개별 예외 클래스의 부모 역할

---

## ErrorCode 체계

### ErrorCode Enum 구조

```java
public enum ErrorCode {
    // ==================== User 도메인 (U로 시작) ====================
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    USER_DUPLICATION("U002", "이미 존재하는 사용자입니다", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("U003", "이메일 혹은 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("U004", "인증이 필요합니다", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("U005", "접근 권한이 없습니다", HttpStatus.FORBIDDEN),

    // ==================== Board 도메인 (B로 시작) ====================
    BOARD_NOT_FOUND("B001", "게시글을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    BOARD_ACCESS_DENIED("B002", "게시글에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),

    // ==================== FAQ 도메인 (F로 시작) ====================
    FAQ_NOT_FOUND("F001", "FAQ를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    FAQ_ACCESS_DENIED("F002", "FAQ에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),

    // ==================== BlogTemplate 도메인 (BT로 시작) ====================
    BLOG_TEMPLATE_NOT_FOUND("BT001", "블로그 템플릿을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    BLOG_TEMPLATE_ACCESS_DENIED("BT002", "블로그 템플릿에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),
    BLOG_TEMPLATE_ALREADY_EXISTS("BT003", "이미 블로그 템플릿이 존재합니다", HttpStatus.CONFLICT),

    // ==================== 공통 (C로 시작) ====================
    INVALID_INPUT("C001", "입력값이 올바르지 않습니다", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST("C002", "잘못된 요청입니다", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("C003", "리소스를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DATA_INTEGRITY_VIOLATION("C004", "데이터 무결성 제약 조건 위반", HttpStatus.CONFLICT),
    METHOD_NOT_ALLOWED("C005", "허용되지 않은 HTTP 메서드입니다", HttpStatus.METHOD_NOT_ALLOWED),
    INTERNAL_SERVER_ERROR("C999", "서버 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
```

### 에러 코드 네이밍 규칙

| Prefix | 도메인 | 범위 | 예시 |
|--------|--------|------|------|
| U | User | U001 ~ U999 | U001: 사용자 미발견 |
| B | Board | B001 ~ B999 | B001: 게시글 미발견 |
| F | FAQ | F001 ~ F999 | F001: FAQ 미발견 |
| BT | BlogTemplate | BT001 ~ BT999 | BT001: 템플릿 미발견 |
| C | Common | C001 ~ C999 | C001: 입력값 오류, C999: 서버 오류 |

### 에러 코드 추가 방법

```java
// 1. ErrorCode enum에 새 코드 추가
USER_EMAIL_INVALID("U006", "이메일 형식이 올바르지 않습니다", HttpStatus.BAD_REQUEST),

// 2. 예외 클래스에서 사용
public static UserException invalidEmail(String email) {
    return new UserException(
        ErrorCode.USER_EMAIL_INVALID,
        String.format("유효하지 않은 이메일입니다: %s", email)
    );
}
```

---

## 코드 흐름

### 1. 예외 발생 → 처리 → 응답 전체 흐름

```
┌─────────────────┐
│   Controller    │
│  (API Endpoint) │
└────────┬────────┘
         │ 요청
         ▼
┌─────────────────┐
│    Service      │
│ (비즈니스 로직)  │
└────────┬────────┘
         │
         │ 예외 발생
         │ throw UserException.notFound(userId)
         ▼
┌─────────────────────────────┐
│      UserException          │
│  extends DomainException    │
│  extends BaseException      │
│  (ErrorCode: USER_NOT_FOUND)│
└────────┬────────────────────┘
         │
         │ 예외 전파
         ▼
┌──────────────────────────────┐
│  GlobalExceptionHandler      │
│  @RestControllerAdvice       │
└────────┬─────────────────────┘
         │
         │ @ExceptionHandler(BaseException.class)
         │ handleBaseException(BaseException e)
         ▼
┌──────────────────────────────┐
│   ErrorCode에서 정보 추출     │
│   - code: "U001"             │
│   - message: "사용자를..."    │
│   - httpStatus: NOT_FOUND    │
└────────┬─────────────────────┘
         │
         │ ErrorResponse 생성
         ▼
┌──────────────────────────────┐
│      ErrorResponse           │
│   {                          │
│     "message": "...",        │
│     "status": 404,           │
│     "code": "U001",          │
│     "timestamp": "...",      │
│     "errors": null           │
│   }                          │
└────────┬─────────────────────┘
         │
         │ HTTP 응답
         ▼
┌──────────────────────────────┐
│        Client                │
│  (Frontend Application)      │
└──────────────────────────────┘
```

### 2. Service Layer 예외 발생 예시

```java
@Service
public class UserService {

    public UserResponse findById(Long userId) {
        // 방법 1: 기존 예외 클래스 사용
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        // 방법 2: 정적 팩토리 메서드 사용 (권장)
        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserException.notFound(userId));

        return UserResponse.from(user);
    }

    public void register(RegisterRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw UserException.duplication(request.email());
        }

        // 회원가입 로직...
    }

    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> UserException.invalidCredentials());

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw UserException.invalidCredentials();
        }

        // 로그인 성공 로직...
    }
}
```

### 3. GlobalExceptionHandler 처리 흐름

```java
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ① 모든 비즈니스 예외를 한 곳에서 처리
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        // ② 예외 정보 로깅
        log.error("{}[{}]: {}",
            e.getClass().getSimpleName(),
            e.getCode(),
            e.getMessage()
        );

        // ③ ErrorCode에서 HTTP 상태와 코드 추출
        return ResponseEntity
            .status(e.getHttpStatus())  // ErrorCode에서 가져온 상태
            .body(ErrorResponse.of(
                e.getMessage(),          // 예외 메시지
                e.getHttpStatusCode(),   // HTTP 상태 코드 숫자
                e.getCode()              // 에러 코드 (U001 등)
            ));
    }

    // ④ Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException e
    ) {
        // 필드별 에러 정보 추출
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ErrorResponse.FieldError(
                error.getField(),
                error.getRejectedValue() != null
                    ? error.getRejectedValue().toString()
                    : "",
                error.getDefaultMessage()
            ))
            .toList();

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(
                "입력값 검증에 실패했습니다.",
                HttpStatus.BAD_REQUEST.value(),
                "C001",
                fieldErrors
            ));
    }

    // ⑤ 예상하지 못한 예외 처리 (Fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected Exception: {}", e.getMessage(), e);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of(
                "서버 내부 오류가 발생했습니다.",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "C999"
            ));
    }
}
```

---

## 주요 컴포넌트

### 1. BaseException

**위치:** `_global/exception/base/BaseException.java`

```java
public abstract class BaseException extends RuntimeException {
    private final ErrorCode errorCode;

    protected BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    protected BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() { return errorCode; }
    public String getCode() { return errorCode.getCode(); }
    public HttpStatus getHttpStatus() { return errorCode.getHttpStatus(); }
}
```

**특징:**
- 모든 비즈니스 예외의 추상 기본 클래스
- ErrorCode를 통한 중앙집중식 에러 정보 관리
- HTTP 상태 코드와 에러 코드를 자동으로 제공

### 2. DomainException

**위치:** `_global/exception/base/DomainException.java`

```java
public abstract class DomainException extends BaseException {
    protected DomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected DomainException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
```

**특징:**
- 비즈니스 도메인 예외의 중간 계층
- BaseException의 모든 기능 상속
- 향후 InfrastructureException과 구분된 처리 가능

### 3. UserException (도메인별 통합 예외)

**위치:** `domain/user/exception/UserException.java`

```java
public class UserException extends DomainException {

    // 정적 팩토리 메서드
    public static UserException notFound(Long userId) {
        return new UserException(
            ErrorCode.USER_NOT_FOUND,
            String.format("사용자를 찾을 수 없습니다. ID: %d", userId)
        );
    }

    public static UserException duplication(String email) {
        return new UserException(
            ErrorCode.USER_DUPLICATION,
            String.format("이미 존재하는 이메일입니다: %s", email)
        );
    }

    public static UserException invalidCredentials() {
        return new UserException(ErrorCode.INVALID_CREDENTIALS);
    }
}
```

**특징:**
- User 도메인의 모든 예외를 대표
- 정적 팩토리 메서드로 간편한 예외 생성
- 기존 개별 예외 클래스와 함께 사용 가능

### 4. ErrorCode Enum

**위치:** `_global/exception/base/ErrorCode.java`

**특징:**
- 모든 에러 코드를 한 곳에서 관리
- 에러 코드, 메시지, HTTP 상태 코드를 함께 관리
- 도메인별 prefix로 체계적 관리

### 5. ErrorResponse DTO

**위치:** `_global/exception/dto/ErrorResponse.java`

```java
public record ErrorResponse(
    String message,
    int status,
    String code,
    LocalDateTime timestamp,
    List<FieldError> errors
) {
    public static ErrorResponse of(String message, int status, String code) {
        return new ErrorResponse(message, status, code, LocalDateTime.now(), null);
    }

    public record FieldError(
        String field,
        String rejectedValue,
        String message
    ) {}
}
```

**특징:**
- Record 타입으로 불변성 보장
- 일관된 에러 응답 형식
- Validation 에러를 위한 FieldError 포함

---

## 사용 가이드

### 1. 새로운 도메인 예외 추가하기

#### Step 1: ErrorCode에 에러 코드 추가

```java
public enum ErrorCode {
    // Board 도메인 추가
    BOARD_NOT_FOUND("B001", "게시글을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    BOARD_ACCESS_DENIED("B002", "게시글에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),
}
```

#### Step 2: 도메인별 통합 예외 클래스 생성

```java
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
}
```

#### Step 3: 개별 예외 클래스 생성 (선택사항)

```java
public class BoardNotFoundException extends BoardException {
    public BoardNotFoundException(Long boardId) {
        super(ErrorCode.BOARD_NOT_FOUND,
              String.format("게시글을 찾을 수 없습니다. ID: %d", boardId));
    }
}
```

#### Step 4: Service에서 사용

```java
@Service
public class BoardService {

    public BoardResponse findById(Long boardId) {
        // 방법 1: 통합 예외 클래스의 정적 팩토리 메서드 사용 (권장)
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> BoardException.notFound(boardId));

        // 방법 2: 개별 예외 클래스 사용
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BoardNotFoundException(boardId));

        return BoardResponse.from(board);
    }
}
```

### 2. 예외 발생 방법

#### 방법 1: 정적 팩토리 메서드 사용 (권장)

```java
// 간결하고 의미가 명확함
throw UserException.notFound(userId);
throw UserException.duplication(email);
throw UserException.unauthorized();
throw BoardException.accessDenied(boardId, userId);
```

#### 방법 2: 개별 예외 클래스 사용

```java
// 기존 방식과의 호환성
throw new UserNotFoundException(userId);
throw new DuplicationUserException(email);
throw new InvalidCredentialsException();
```

#### 방법 3: 직접 생성 (특수한 경우)

```java
// 매우 특수한 케이스에만 사용
throw new UserException(ErrorCode.USER_NOT_FOUND, "커스텀 메시지");
```

### 3. Optional과 함께 사용하기

```java
// orElseThrow와 함께 사용
User user = userRepository.findById(userId)
    .orElseThrow(() -> UserException.notFound(userId));

Board board = boardRepository.findByIdAndUserId(boardId, userId)
    .orElseThrow(() -> BoardException.accessDenied(boardId, userId));

// filter와 함께 사용
User activeUser = userRepository.findById(userId)
    .filter(User::isActive)
    .orElseThrow(() -> UserException.unauthorized("비활성화된 계정입니다"));
```

---

## 응답 형식

### 1. 일반 비즈니스 예외 응답

#### 사용자를 찾을 수 없을 때 (404)

**요청:**
```http
GET /api/users/999
```

**응답:**
```json
{
    "message": "사용자를 찾을 수 없습니다. ID: 999",
    "status": 404,
    "code": "U001",
    "timestamp": "2024-11-23T14:30:45.123456",
    "errors": null
}
```

#### 이메일 중복 (409)

**요청:**
```http
POST /api/users/register
{
    "email": "existing@email.com",
    "password": "password123"
}
```

**응답:**
```json
{
    "message": "이미 사용 중인 이메일입니다: existing@email.com",
    "status": 409,
    "code": "U002",
    "timestamp": "2024-11-23T14:30:45.123456",
    "errors": null
}
```

#### 로그인 실패 (401)

**요청:**
```http
POST /api/users/login
{
    "email": "user@email.com",
    "password": "wrongpassword"
}
```

**응답:**
```json
{
    "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
    "status": 401,
    "code": "U003",
    "timestamp": "2024-11-23T14:30:45.123456",
    "errors": null
}
```

### 2. Validation 예외 응답 (400)

**요청:**
```http
POST /api/users/register
{
    "email": "invalid-email",
    "password": "123"
}
```

**응답:**
```json
{
    "message": "입력값 검증에 실패했습니다.",
    "status": 400,
    "code": "C001",
    "timestamp": "2024-11-23T14:30:45.123456",
    "errors": [
        {
            "field": "email",
            "rejectedValue": "invalid-email",
            "message": "이메일 형식이 올바르지 않습니다"
        },
        {
            "field": "password",
            "rejectedValue": "123",
            "message": "비밀번호는 최소 8자 이상이어야 합니다"
        }
    ]
}
```

### 3. 서버 내부 오류 (500)

**응답:**
```json
{
    "message": "서버 내부 오류가 발생했습니다.",
    "status": 500,
    "code": "C999",
    "timestamp": "2024-11-23T14:30:45.123456",
    "errors": null
}
```

---

## 확장 가이드

### 1. Infrastructure 예외 추가

향후 데이터베이스, 외부 API 등 인프라 관련 예외가 필요한 경우:

```java
// 1. InfrastructureException 생성
public abstract class InfrastructureException extends BaseException {
    protected InfrastructureException(ErrorCode errorCode) {
        super(errorCode);
    }
}

// 2. ErrorCode 추가 (I prefix)
DATABASE_CONNECTION_ERROR("I001", "데이터베이스 연결 오류", HttpStatus.INTERNAL_SERVER_ERROR),
EXTERNAL_API_TIMEOUT("I002", "외부 API 타임아웃", HttpStatus.GATEWAY_TIMEOUT),

// 3. 구체적 예외 클래스
public class DatabaseException extends InfrastructureException {
    public static DatabaseException connectionFailed() {
        return new DatabaseException(ErrorCode.DATABASE_CONNECTION_ERROR);
    }
}

// 4. GlobalExceptionHandler에 별도 핸들러 추가 (선택)
@ExceptionHandler(InfrastructureException.class)
public ResponseEntity<ErrorResponse> handleInfraException(InfrastructureException e) {
    log.error("Infrastructure error: ", e);
    // 보안상 상세 메시지 숨김
    return ResponseEntity
        .status(500)
        .body(ErrorResponse.of("시스템 오류가 발생했습니다", 500, "C999"));
}
```

### 2. 국제화(i18n) 지원

```java
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
        BaseException e,
        HttpServletRequest request
    ) {
        Locale locale = request.getLocale();
        String localizedMessage = messageSource.getMessage(
            e.getCode(),
            null,
            e.getMessage(),
            locale
        );

        return ResponseEntity
            .status(e.getHttpStatus())
            .body(ErrorResponse.of(localizedMessage, e.getHttpStatusCode(), e.getCode()));
    }
}
```

**messages_ko.properties:**
```properties
U001=사용자를 찾을 수 없습니다
U002=이미 존재하는 사용자입니다
U003=인증 정보가 올바르지 않습니다
```

**messages_en.properties:**
```properties
U001=User not found
U002=User already exists
U003=Invalid credentials
```

### 3. 에러 추적 ID 추가

```java
public record ErrorResponse(
    String message,
    int status,
    String code,
    LocalDateTime timestamp,
    List<FieldError> errors,
    String traceId  // 추적 ID 추가
) {
    public static ErrorResponse of(String message, int status, String code) {
        return new ErrorResponse(
            message,
            status,
            code,
            LocalDateTime.now(),
            null,
            UUID.randomUUID().toString()  // 고유 추적 ID
        );
    }
}
```

---

## 베스트 프랙티스

### ✅ DO (권장)

1. **정적 팩토리 메서드 사용**
   ```java
   throw UserException.notFound(userId);
   ```

2. **ErrorCode enum 활용**
   ```java
   USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND)
   ```

3. **명확한 에러 메시지**
   ```java
   String.format("사용자를 찾을 수 없습니다. ID: %d", userId)
   ```

4. **도메인별 예외 그룹화**
   ```java
   UserException, BoardException, FaqException
   ```

5. **보안 고려**
   ```java
   // 로그인 실패 시 이메일/비밀번호 구분하지 않음
   throw UserException.invalidCredentials();
   ```

### ❌ DON'T (비권장)

1. **RuntimeException 직접 사용**
   ```java
   throw new RuntimeException("사용자를 찾을 수 없습니다");  // ❌
   ```

2. **에러 코드 하드코딩**
   ```java
   ErrorResponse.of("에러", 404, "USER_NOT_FOUND");  // ❌
   ```

3. **핸들러에 HTTP 상태 코드 하드코딩**
   ```java
   @ResponseStatus(HttpStatus.NOT_FOUND)  // ❌ ErrorCode에서 관리
   ```

4. **예외마다 별도 핸들러 생성**
   ```java
   @ExceptionHandler(UserNotFoundException.class)  // ❌ BaseException으로 통합
   ```

5. **민감한 정보 노출**
   ```java
   String.format("비밀번호가 틀렸습니다: %s", password);  // ❌
   ```

---

## 참고 자료

- [Spring Boot 3.x Exception Handling](https://spring.io/guides/tutorials/rest/)
- [RFC 7807 - Problem Details for HTTP APIs](https://tools.ietf.org/html/rfc7807)
- [Clean Code - Error Handling](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882)

---

## 문서 이력

| 날짜 | 작성자 | 내용 |
|------|--------|------|
| 2024-11-23 | cryschan | 초기 문서 작성 |
| 2025-11-25 | - | ErrorCode 업데이트 (U003 메시지, BT001~BT003, C002~C005 추가) |
