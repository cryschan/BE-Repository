# 마이페이지 백엔드 구현 계획서 (v1/v2/확장)

## 📋 현재 상태 분석

### 기존 코드 현황
- **ProfileController**: 부분 구현 (GET /api/v1/profile 엔드포인트만 정의)
- **UserProfileService**: 메소드 시그니처만 존재, 구현 필요
- **User Entity**: 기본 필드 구현 완료 (userId, email, username, role, department, tokenUsage)
- **UserService**: 회원가입/로그인 기능만 구현 (JWT 미구현)
- **UserRepository**: findByEmail 메소드만 구현

---

## 🎯 v1: 기본 마이페이지 기능 (우선 구현)

### 1.1 보안 인프라 구축
**JWT 토큰 시스템 (필수)**
```
📁 src/main/java/.../security/jwt/
  ├── JwtTokenProvider.java       # JWT 생성/검증
  ├── JwtAuthenticationFilter.java # 요청 필터링
  └── JwtProperties.java          # JWT 설정값
```

**Spring Security 설정**
```
📁 src/main/java/.../config/
  └── SecurityConfig.java          # Security 설정
```

### 1.2 User Domain 확장

#### Entity 현황
```java
@Entity
@Table(name = "users")
public class User {
    // 현재 구현된 필드
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true)
    private String email;
    private String password;
    private String username;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String department;
    private Long tokenUsage;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 추후 구현 예정 필드
    // private LocalDateTime lastLoginAt;
    // private boolean isActive = true;
}
```

#### API 엔드포인트 (v1)
| Method | Endpoint | 설명 | 상태 |
|--------|----------|------|------|
| GET | /api/user-profile | 내 프로필 조회 | ✅ 구현 완료 |
| GET | /api/user-profile/{targetUserId} | 다른 사용자 프로필 조회 (관리자) | ✅ 구현 완료 |
| PUT | /api/user-profile/update | 프로필 기본정보 수정 | ✅ 구현 완료 |
| PATCH | /api/user-profile/password | 비밀번호 변경 | 🔜 추후 구현 예정 |

#### DTO 클래스 (v1)
```java
// Request (구현 완료)
public record UpdateProfileRequest(
    @NotBlank(message = "사용자 이름은 필수입니다")
    String username,
    String department
) {}

// Request (추후 구현 예정)
public record PasswordChangeRequest(
    String currentPassword,
    String newPassword,
    String confirmPassword
) {}

// Response (구현 완료)
public record UserDetailResponse(
    Long userId,
    String email,
    String username,
    String department,
    UserRole role,
    Long tokenUsage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

#### Service 구현 (v1)
```java
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserProfileService {

    private final UserRepository userRepository;

    // 자기 프로필 조회 (구현 완료)
    public UserDetailResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound(userId));
        return UserDetailResponse.from(user);
    }

    // 다른 사용자 조회 - 관리자 전용 (구현 완료)
    public UserDetailResponse getUserProfile(Long userId, Long targetUserId) {
        User admin = userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound(userId));
        if (admin.getRole() != UserRole.ADMIN) {
            throw UserException.accessDenied();
        }
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> UserException.notFound(targetUserId));
        return UserDetailResponse.from(targetUser);
    }

    // 프로필 업데이트 (구현 완료)
    @Transactional
    public UserDetailResponse updateMyProfile(Long userId, UpdateProfileRequest request) {
        User updateUser = userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound(userId));
        updateUser.updateProfile(request.username(), request.department());
        return UserDetailResponse.from(updateUser);
    }

    // 비밀번호 변경 (추후 구현 예정)
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        // TODO: 비밀번호 변경 로직 구현
    }
}
```

> 💡 **참고**: Controller에서 `@AuthenticationPrincipal Long userId`로 JWT에서 추출한 사용자 ID를 받습니다.

### 1.3 v1 구현 현황

**Phase 1: 보안 기반** ✅ 완료
- [x] JWT 의존성 추가
- [x] JwtUtil 구현 (Access Token + Refresh Token)
- [x] JwtAuthenticationFilter 구현
- [x] Spring Security 설정
- [x] 기존 로그인 API와 JWT 통합
- [x] Refresh Token 기반 토큰 갱신/로그아웃

**Phase 2: 마이페이지 핵심** ✅ 완료
- [x] UserDetailResponse DTO 생성
- [x] UpdateProfileRequest DTO 생성
- [x] 프로필 조회 API 구현 (GET /api/user-profile)
- [x] 다른 사용자 프로필 조회 API 구현 (GET /api/user-profile/{id}, 관리자)
- [x] 프로필 수정 API 구현 (PUT /api/user-profile/update)
- [ ] 비밀번호 변경 API 구현 (추후 예정)

---

## 🚀 v2: 고급 마이페이지 기능 (차후 구현)

### 2.1 데이터베이스 확장

**user_activities 테이블 (활동 추적)**
```sql
CREATE TABLE user_activities (
    activity_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    description TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

**refresh_tokens 테이블 (고급 토큰 관리)**
```sql
CREATE TABLE refresh_tokens (
    token_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

### 2.2 User Entity 추가 확장 (v2)
```java
@Entity
public class User {
    // v1 필드들...
    
    // v2 추가 필드
    private String profileImageUrl;  // 프로필 이미지
    private String bio;              // 자기소개
    private String phoneNumber;      // 전화번호
    
    // 연관 관계
    @OneToMany(mappedBy = "user")
    private List<UserActivity> activities;
}
```

### 2.3 API 엔드포인트 (v2)
| Method | Endpoint | 설명 | 도메인 연계 |
|--------|----------|------|------------|
| GET | /api/v1/profile/activities | 활동 내역 조회 | Analytics |
| GET | /api/v1/profile/token-usage | 토큰 사용량 통계 | Analytics |
| POST | /api/v1/profile/image | 프로필 이미지 업로드 | File |
| DELETE | /api/v1/profile/image | 프로필 이미지 삭제 | File |

### 2.4 고급 기능 구현

#### 활동 추적 서비스
```java
@Service
public class UserActivityService {
    
    public void logActivity(User user, ActivityType type, String description) {
        UserActivity activity = UserActivity.builder()
            .user(user)
            .activityType(type)
            .description(description)
            .ipAddress(getClientIp())
            .build();
        activityRepository.save(activity);
    }
    
    public Page<UserActivityResponse> getUserActivities(Long userId, Pageable pageable) {
        return activityRepository.findByUserId(userId, pageable)
            .map(UserActivityResponse::from);
    }
}
```

#### 파일 업로드 서비스
```java
@Service
public class FileStorageService {
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    public String uploadProfileImage(MultipartFile file, String userId) {
        validateImageFile(file);
        String fileName = generateFileName(file, userId);
        Path targetLocation = Paths.get(uploadDir)
            .resolve("profiles")
            .resolve(userId)
            .resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation);
        return "/uploads/profiles/" + userId + "/" + fileName;
    }
}
```

#### RefreshToken 관리
```java
@Service
public class RefreshTokenService {
    
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(generateUniqueToken())
            .expiresAt(LocalDateTime.now().plusDays(7))
            .build();
        return refreshTokenRepository.save(refreshToken);
    }
    
    public TokenResponse refreshAccessToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new InvalidTokenException());
        
        if (token.isExpired()) {
            throw new ExpiredTokenException();
        }
        
        return generateNewAccessToken(token.getUser());
    }
}
```

### 2.5 v2 구현 일정

**Phase 1: 활동 추적 (1주)**
- [ ] user_activities 테이블 생성
- [ ] UserActivity 엔티티 구현
- [ ] 활동 로깅 서비스 구현
- [ ] 활동 내역 조회 API

**Phase 2: 파일 처리 (1주)**
- [ ] 파일 업로드 서비스 구현
- [ ] 이미지 검증 로직
- [ ] 프로필 이미지 API
- [ ] User 엔티티에 profileImageUrl 추가

**Phase 3: 고급 토큰 관리 (1주)**
- [ ] refresh_tokens 테이블 생성
- [ ] RefreshToken 서비스 구현
- [ ] 토큰 갱신 API
- [ ] 토큰 사용량 통계 API

---

## 🔮 추후 확장 가능 기능

### 3.1 계정 관리
- **계정 삭제 (소프트 삭제)**
  - deleted_at 필드 활용
  - 30일 유예기간 후 완전 삭제
  - 관련 데이터 백업 기능

- **계정 복구**
  - 삭제 유예기간 내 복구
  - 이메일 인증 필요

### 3.2 소셜 기능
- **소셜 로그인 연동**
  - OAuth2 구현 (Google, Kakao, Naver)
  - Spring Security OAuth2 Client 활용
  - 계정 연동/해제 관리

- **프로필 공개 설정**
  - 공개/비공개 설정
  - 공개 프로필 URL 생성
  - 프로필 조회 권한 관리

### 3.3 데이터 관리
- **개인 데이터 다운로드**
  - GDPR 대응
  - JSON/CSV 형식 지원
  - 비동기 처리 (대용량 데이터)

- **데이터 이전**
  - 계정 간 데이터 이전
  - 외부 서비스로 내보내기

### 3.4 알림 설정
- **알림 채널 관리**
  - 이메일/SMS/푸시 알림 설정
  - 알림 유형별 on/off
  - 알림 시간대 설정

- **알림 템플릿**
  - 사용자별 맞춤 템플릿
  - 언어 설정

### 3.5 보안 강화
- **2단계 인증 (2FA)**
  - TOTP 기반 (Google Authenticator)
  - SMS 인증
  - 백업 코드 발급

- **보안 로그**
  - 로그인 이력 조회
  - 의심스러운 활동 감지
  - IP 기반 접근 제한

- **세션 관리**
  - 다중 기기 로그인 관리
  - 원격 로그아웃
  - 세션 만료 정책

### 3.6 성능 최적화
- **Rate Limiting**
  - API 호출 제한
  - 사용자별/IP별 제한
  - Bucket4j 또는 Redis 활용

- **캐싱**
  - 프로필 정보 캐싱
  - Redis 기반 세션 관리
  - CDN 연동 (이미지)

### 3.7 분석 및 리포트
- **사용 패턴 분석**
  - 로그인 패턴 분석
  - 기능 사용 통계
  - 사용자 행동 분석

- **월간 리포트**
  - 활동 요약 이메일
  - 토큰 사용량 리포트
  - 보안 알림 요약

---

## 📅 전체 구현 로드맵

### 단기 (v1) - 2주
1. **Week 1**: JWT/Security 기반 구축
2. **Week 2**: 마이페이지 기본 기능 구현

### 중기 (v2) - 3주
1. **Week 3**: 활동 추적 시스템
2. **Week 4**: 파일 업로드 처리
3. **Week 5**: 고급 토큰 관리

### 장기 (확장) - 필요시 구현
- 우선순위와 비즈니스 요구사항에 따라 선택적 구현
- 사용자 피드백 기반 기능 추가
- 규정 준수 (GDPR 등) 관련 기능

---

## 🚨 주요 고려사항

### v1 고려사항
- JWT 시크릿 키 안전한 관리
- 비밀번호 정책 적용 (최소 8자, 복잡도)
- 기본적인 입력 검증
- 에러 메시지 표준화

### v2 고려사항
- 파일 업로드 보안 (타입, 크기 제한)
- 활동 로그 데이터 증가 대비
- RefreshToken 만료 정책
- 이미지 최적화 (리사이징, 압축)

### 확장 시 고려사항
- 외부 서비스 의존성 관리
- 데이터 프라이버시 규정 준수
- 확장성 있는 아키텍처 설계
- 마이크로서비스 전환 가능성

---

## 📚 필요 의존성

### v1 의존성
```gradle
dependencies {
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    
    // Spring Security
    implementation 'org.springframework.boot:spring-boot-starter-security'
    
    // Validation
    implementation 'org.springframework.boot:spring-boot-starter-validation'
}
```

### v2 추가 의존성
```gradle
dependencies {
    // File Upload
    implementation 'commons-io:commons-io:2.11.0'
    
    // 이미지 처리
    implementation 'net.coobird:thumbnailator:0.4.19'
}
```

### 확장 시 추가 의존성
```gradle
dependencies {
    // OAuth2 (소셜 로그인)
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    
    // 2FA
    implementation 'com.warrenstrange:googleauth:1.5.0'
    
    // Rate Limiting
    implementation 'com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0'
    
    // Redis (캐싱, 세션)
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
}
```

---

## ✅ 다음 단계

1. **즉시 시작 (v1)**
   - JWT 의존성 추가
   - SecurityConfig 생성
   - JwtTokenProvider 구현

2. **v1 완료 후 (v2)**
   - 활동 추적 테이블 설계
   - 파일 업로드 인프라 구축
   - RefreshToken 로직 구현

3. **비즈니스 요구사항 기반 (확장)**
   - 사용자 피드백 수집
   - 우선순위 결정
   - 단계적 구현