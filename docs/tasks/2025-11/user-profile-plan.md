# 유저 프로필 조회 기능 개발 계획서

## 📋 문서 정보
- **작성일**: 2025-11-22
- **기능**: 유저 프로필 조회 (GET /api/users/me)
- **개발 방식**: TDD (Test-Driven Development)

---

## 1. 현재 상황 분석

### ✅ 이미 준비된 컴포넌트

| 컴포넌트 | 상태 | 설명 |
|---------|------|------|
| `User` 엔티티 | ✅ 완성 | userId, email, username, department, role, tokenUsage, createdAt, updatedAt |
| `UserDetailResponse` DTO | ✅ 완성 | 마이페이지 조회용 응답 DTO, from() 정적 메서드 포함 |
| `UserRepository` | ✅ 완성 | findById는 JpaRepository에서 제공 |
| `JwtUtil` | ✅ 완성 | JWT 토큰 검증 및 userId 추출 기능 |
| `JwtAuthenticationFilter` | ✅ 존재 | JWT 인증 필터 |

### ⚠️ 구현이 필요한 컴포넌트

| 컴포넌트 | 상태 | 작업 필요 사항 |
|---------|------|---------------|
| `UserProfileService` | 🔴 빈 클래스 | getMyProfile() 메서드 구현 |
| `ProfileController` | 🔴 빈 클래스 | GET /api/users/me 엔드포인트 구현 |
| `UserProfileServiceTest` | 🔴 주석만 존재 | 테스트 케이스 구현 |

---

## 2. API 명세

### 2.1 엔드포인트

```
GET /api/users/me
```

### 2.2 요청

**Headers:**
```
Authorization: Bearer {JWT_TOKEN}
```

**Body:** 없음

### 2.3 응답

**Success (200 OK):**
```json
{
  "userId": 1,
  "email": "user@example.com",
  "username": "홍길동",
  "department": "개발팀",
  "role": "USER",
  "tokenUsage": 1000,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-15T14:30:00"
}
```

**Error Responses:**
```
401 Unauthorized - JWT 토큰 없음 또는 유효하지 않음
404 Not Found - 사용자를 찾을 수 없음 (UserNotFoundException)
```

---

## 3. 구현 계획 (TDD 방식)

### Phase 1: 테스트 코드 작성 ✍️

#### UserProfileServiceTest 구현

**테스트 환경 설정:**
```java
@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    // 테스트용 User 엔티티 생성
    private User createTestUser() {
        return User.builder()
            .email("test@example.com")
            .password("encodedPassword")
            .username("테스트유저")
            .department("개발팀")
            .role(UserRole.USER)
            .build();
    }
}
```

**테스트 시나리오:**

1. **✅ 정상 케이스: 인증된 사용자가 자신의 프로필 조회**
   ```java
   @Test
   @DisplayName("유저 프로필 조회 성공")
   void getMyProfile_Success() {
       // Given: 존재하는 userId
       Long userId = 1L;
       User user = createTestUser();
       when(userRepository.findById(userId)).thenReturn(Optional.of(user));

       // When: getMyProfile(userId) 호출
       UserDetailResponse response = userProfileService.getMyProfile(userId);

       // Then: UserDetailResponse 반환
       assertNotNull(response);
       assertEquals(user.getEmail(), response.email());
       assertEquals(user.getUsername(), response.username());
       verify(userRepository).findById(userId);
   }
   ```

2. **❌ 예외 케이스: 존재하지 않는 userId**
   ```java
   @Test
   @DisplayName("존재하지 않는 유저 조회 시 UserNotFoundException 발생")
   void getMyProfile_UserNotFound() {
       // Given: 존재하지 않는 userId
       Long userId = 999L;
       when(userRepository.findById(userId)).thenReturn(Optional.empty());

       // When & Then: UserNotFoundException 발생
       assertThrows(UserNotFoundException.class,
           () -> userProfileService.getMyProfile(userId));
       verify(userRepository).findById(userId);
   }
   ```

3. **❌ 예외 케이스: userId가 null (선택사항)**
   ```java
   @Test
   @DisplayName("userId가 null일 때 IllegalArgumentException 발생")
   void getMyProfile_NullUserId() {
       // Given: userId = null
       Long userId = null;

       // When & Then: IllegalArgumentException 발생
       assertThrows(IllegalArgumentException.class,
           () -> userProfileService.getMyProfile(userId));
   }
   ```

---

### Phase 2: Service 구현 🔧

#### UserProfileService.getMyProfile() 구현

**파일 위치:** `src/main/java/io/github/cryschan/berepository/domain/user/service/UserProfileService.java`

**구현 코드:**
```java
package io.github.cryschan.berepository.domain.user.service;

import io.github.cryschan.berepository.domain.user.dto.response.UserDetailResponse;
import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.exception.UserNotFoundException;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserProfileService {

    private final UserRepository userRepository;

    /**
     * 사용자 프로필 조회
     *
     * @param userId 조회할 사용자 ID (JWT에서 추출)
     * @return UserDetailResponse 사용자 상세 정보
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    public UserDetailResponse getMyProfile(Long userId) {
        // userId로 User 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        // Entity -> DTO 변환
        return UserDetailResponse.from(user);
    }
}
```

#### UserNotFoundException 수정 (필요시)

**확인 사항:**
- `UserNotFoundException`에 userId를 받는 생성자가 있는지 확인
- 없다면 추가 구현 필요

**예시:**
```java
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("사용자를 찾을 수 없습니다. userId: " + userId);
    }
}
```

---

### Phase 3: Controller 구현 🎯

#### ProfileController 구현

**파일 위치:** `src/main/java/io/github/cryschan/berepository/domain/user/controller/ProfileController.java`

**구현 방법 A: @AuthenticationPrincipal 사용 (SecurityContext에 저장된 경우)**
```java
package io.github.cryschan.berepository.domain.user.controller;

import io.github.cryschan.berepository.domain.user.dto.response.UserDetailResponse;
import io.github.cryschan.berepository.domain.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "사용자 프로필 관리 API")
public class ProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "인증된 사용자의 상세 정보를 조회합니다.")
    public ResponseEntity<UserDetailResponse> getMyProfile(
        @AuthenticationPrincipal Long userId
    ) {
        UserDetailResponse response = userProfileService.getMyProfile(userId);
        return ResponseEntity.ok(response);
    }
}
```

**구현 방법 B: HttpServletRequest에서 직접 추출 (대안)**
```java
@GetMapping("/me")
@Operation(summary = "내 정보 조회", description = "인증된 사용자의 상세 정보를 조회합니다.")
public ResponseEntity<UserDetailResponse> getMyProfile(HttpServletRequest request) {
    String token = extractToken(request);
    Long userId = jwtUtil.getUserId(token);

    UserDetailResponse response = userProfileService.getMyProfile(userId);
    return ResponseEntity.ok(response);
}

private String extractToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
        return bearerToken.substring(7);
    }
    throw new UnauthorizedException("JWT 토큰이 없습니다.");
}
```

**구현 방법 C: 커스텀 어노테이션 사용 (권장)**
```java
// 1. 어노테이션 생성
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}

// 2. ArgumentResolver 구현
@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {
    private final JwtUtil jwtUtil;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class);
    }

    @Override
    public Object resolveArgument(...) {
        String token = extractToken(request);
        return jwtUtil.getUserId(token);
    }
}

// 3. Controller에서 사용
@GetMapping("/me")
public ResponseEntity<UserDetailResponse> getMyProfile(@CurrentUserId Long userId) {
    UserDetailResponse response = userProfileService.getMyProfile(userId);
    return ResponseEntity.ok(response);
}
```

---

### Phase 4: 통합 테스트 🧪 (선택사항)

#### Controller 통합 테스트

**테스트 환경:**
```java
@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class) // 필요시
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userProfileService;

    @MockBean
    private JwtUtil jwtUtil;
}
```

**테스트 시나리오:**

1. **정상 케이스: JWT 포함하여 조회**
```java
@Test
@DisplayName("GET /api/users/me - 성공")
void getMyProfile_Success() throws Exception {
    // Given
    String validToken = "valid.jwt.token";
    Long userId = 1L;
    UserDetailResponse response = new UserDetailResponse(...);

    when(jwtUtil.getUserId(validToken)).thenReturn(userId);
    when(userProfileService.getMyProfile(userId)).thenReturn(response);

    // When & Then
    mockMvc.perform(get("/api/users/me")
            .header("Authorization", "Bearer " + validToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.email").exists());
}
```

2. **실패 케이스: JWT 없음**
```java
@Test
@DisplayName("GET /api/users/me - JWT 없음 (401)")
void getMyProfile_NoToken() throws Exception {
    mockMvc.perform(get("/api/users/me"))
        .andExpect(status().isUnauthorized());
}
```

3. **실패 케이스: 잘못된 JWT**
```java
@Test
@DisplayName("GET /api/users/me - 잘못된 JWT (401)")
void getMyProfile_InvalidToken() throws Exception {
    String invalidToken = "invalid.token";

    when(jwtUtil.getUserId(invalidToken))
        .thenThrow(new JwtException("Invalid token"));

    mockMvc.perform(get("/api/users/me")
            .header("Authorization", "Bearer " + invalidToken))
        .andExpect(status().isUnauthorized());
}
```

---

## 4. 기술적 고려사항

### 4.1 JWT에서 userId 추출 방법

**확인 필요:**
- `JwtAuthenticationFilter`에서 SecurityContext에 userId를 저장하는지?
- `@AuthenticationPrincipal` 사용 가능한지?

**권장 방법:**
1. **커스텀 어노테이션 `@CurrentUserId`** (가독성 ↑, 재사용성 ↑)
2. `@AuthenticationPrincipal` (Spring Security 표준)
3. `HttpServletRequest`에서 직접 추출 (간단하지만 중복 코드 발생 가능)

### 4.2 본인 확인 로직

**설계 특성:**
- 현재 설계상 "자기 자신만 조회 가능"
- JWT의 userId == 조회하려는 userId (항상 동일)
- **별도 권한 체크 불필요** (JWT 인증만으로 충분)

### 4.3 예외 처리

**필요한 예외:**
- `UserNotFoundException`: 이미 존재함 (userId 생성자 확인 필요)
- `UnauthorizedException`: JWT 관련 예외 (필요시)

**전역 예외 처리:**
- `@ControllerAdvice`에서 처리 확인
- 표준 에러 응답 형식 적용

### 4.4 응답 형식

**현재 설계:**
- `UserDetailResponse` 직접 반환

**확인 필요:**
- 공통 응답 래퍼가 있는지? (예: `ApiResponse<UserDetailResponse>`)
- 없다면 현재대로 직접 반환

---

## 5. 구현 순서 (권장)

### 단계별 체크리스트

- [ ] **1단계: 사전 확인**
  - [ ] `UserNotFoundException`에 userId 생성자 확인/추가
  - [ ] `JwtAuthenticationFilter` 동작 방식 확인
  - [ ] SecurityContext에 userId 저장 여부 확인

- [ ] **2단계: 테스트 코드 작성 (TDD)**
  - [ ] `UserProfileServiceTest` 클래스 구현
  - [ ] 정상 케이스 테스트
  - [ ] 예외 케이스 테스트 (UserNotFoundException)
  - [ ] 예외 케이스 테스트 (null userId, 선택)

- [ ] **3단계: Service 구현**
  - [ ] `UserProfileService.getMyProfile()` 메서드 구현
  - [ ] 테스트 실행 및 통과 확인

- [ ] **4단계: Controller 구현**
  - [ ] `ProfileController` 구현
  - [ ] JWT에서 userId 추출 방식 선택 및 적용
  - [ ] Swagger 문서화 (@Operation, @Tag)

- [ ] **5단계: 통합 테스트 (선택)**
  - [ ] `ProfileControllerTest` 작성
  - [ ] Mock 기반 Controller 테스트

- [ ] **6단계: 수동 테스트**
  - [ ] Postman 또는 Swagger UI로 테스트
  - [ ] JWT 토큰 발급 후 API 호출
  - [ ] 정상 응답 확인
  - [ ] 예외 상황 테스트

- [ ] **7단계: 문서화 및 코드 리뷰**
  - [ ] API 문서 업데이트 (필요시)
  - [ ] 코드 리뷰 요청

---

## 6. 확인이 필요한 사항

### 우선순위 높음 🔴

1. **JWT 인증 방식**
   - [ ] SecurityContext에 userId가 저장되는지?
   - [ ] `@AuthenticationPrincipal` 사용 가능한지?
   - [ ] 현재 프로젝트의 인증 처리 방식은?

2. **예외 클래스**
   - [ ] `UserNotFoundException`에 userId를 받는 생성자가 있는지?
   - [ ] 없다면 추가 구현 필요

### 우선순위 중간 🟡

3. **공통 응답 형식**
   - [ ] 프로젝트에 공통 응답 래퍼가 있는지? (예: `ApiResponse<T>`)
   - [ ] 다른 API의 응답 형식은?

4. **기존 테스트 패턴**
   - [ ] 다른 Service 테스트 코드가 있다면 그 패턴 따라가기
   - [ ] Mockito 사용 방식 확인

---

## 7. 예상 파일 변경 목록

### 필수 수정 파일 ✏️

```
src/main/java/io/github/cryschan/berepository/domain/user/
├── service/
│   └── UserProfileService.java           (메서드 구현)
├── controller/
│   └── ProfileController.java            (API 엔드포인트 구현)
└── test/java/.../domain/user/service/
    └── UserProfileServiceTest.java       (테스트 코드 작성)
```

### 조건부 수정 파일 ❓

```
src/main/java/io/github/cryschan/berepository/domain/user/
├── exception/
│   └── UserNotFoundException.java        (생성자 추가 필요시)
└── _global/
    ├── jwt/
    │   └── JwtAuthenticationFilter.java  (SecurityContext 설정 확인)
    └── resolver/
        └── CurrentUserIdArgumentResolver.java  (커스텀 어노테이션 사용 시)
```

### 선택적 추가 파일 💡

```
src/main/java/io/github/cryschan/berepository/_global/
└── annotation/
    └── CurrentUserId.java                (커스텀 어노테이션)
```

---

## 8. 테스트 계획

### 8.1 단위 테스트 (UserProfileServiceTest)

| 테스트 케이스 | 예상 결과 | 우선순위 |
|-------------|---------|---------|
| 정상적인 userId로 조회 | UserDetailResponse 반환 | 🔴 High |
| 존재하지 않는 userId | UserNotFoundException | 🔴 High |
| null userId | IllegalArgumentException | 🟡 Medium |

### 8.2 통합 테스트 (ProfileControllerTest)

| 테스트 케이스 | 예상 결과 | 우선순위 |
|-------------|---------|---------|
| 유효한 JWT 토큰으로 조회 | 200 OK + UserDetailResponse | 🔴 High |
| JWT 토큰 없이 조회 | 401 Unauthorized | 🔴 High |
| 잘못된 JWT 토큰 | 401 Unauthorized | 🟡 Medium |
| 만료된 JWT 토큰 | 401 Unauthorized | 🟡 Medium |

### 8.3 수동 테스트 (Postman/Swagger)

1. **회원가입** → JWT 토큰 획득
2. **로그인** → JWT 토큰 갱신 (필요시)
3. **프로필 조회** → 토큰 포함하여 `GET /api/users/me`
4. **잘못된 토큰** → 401 확인
5. **토큰 없음** → 401 확인

---

## 9. 완료 기준 (Definition of Done)

- [ ] 모든 단위 테스트 통과 (커버리지 80% 이상)
- [ ] 통합 테스트 통과 (선택사항)
- [ ] Swagger 문서 자동 생성 확인
- [ ] Postman/Swagger UI로 수동 테스트 성공
- [ ] 코드 리뷰 완료
- [ ] 불필요한 주석 제거
- [ ] 코드 포맷팅 완료 (IntelliJ: ⌥⌘L)
- [ ] 불필요한 import 제거 (IntelliJ: ⌃⌥O)

---

## 10. 참고 자료

### 관련 문서
- [DOMAIN_ARCHITECTURE.md](../../document/DOMAIN_ARCHITECTURE.md) - User Domain 섹션 (4.1)
- Spring Security JWT 인증 가이드
- Spring Data JPA Repository 문서

### 기존 코드
- `UserService.login()` - JWT 생성 예시
- `JwtUtil.getUserId()` - JWT에서 userId 추출 예시

---

## 부록: 예상 문제 및 해결 방안

### 문제 1: @AuthenticationPrincipal이 작동하지 않음

**원인:** SecurityContext에 userId가 저장되지 않음

**해결:**
- `JwtAuthenticationFilter`에서 `SecurityContextHolder.getContext().setAuthentication()` 추가
- 또는 커스텀 어노테이션 `@CurrentUserId` 사용

### 문제 2: UserNotFoundException 생성자 오류

**원인:** userId를 받는 생성자가 없음

**해결:**
```java
public UserNotFoundException(Long userId) {
    super("사용자를 찾을 수 없습니다. userId: " + userId);
}
```

### 문제 3: 테스트에서 Mock이 작동하지 않음

**원인:** `@ExtendWith(MockitoExtension.class)` 누락

**해결:**
- 테스트 클래스에 어노테이션 추가
- `@Mock`, `@InjectMocks` 확인

---

**문서 종료**
