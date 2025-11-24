# JWT 인증된 유저의 블로그 글 조회 가이드

## 목차
1. [현재 구현된 코드](#현재-구현된-코드)
2. [JWT 토큰 처리 흐름](#jwt-토큰-처리-흐름)
3. [Spring Security Principal 처리 방식 비교](#spring-security-principal-처리-방식-비교)
4. [TODO 해결 방법](#todo-해결-방법)
5. [모범 사례: CustomUserDetails 구현](#모범-사례-customuserdetails-구현)

---

## 현재 구현된 코드

### 1. Controller Layer

**위치**: `BlogController.java`

```java
@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
@Tag(name = "Blog", description = "블로그 API")
public class BlogController {
    
    private final BlogService blogService;

    @GetMapping("/my")
    @Operation(summary = "내 블로그 글 조회", description = "인증된 사용자의 블로그 글을 페이지네이션하여 조회합니다.")
    public ResponseEntity<BlogPageResponse> getMyBlogs(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "1") int page
    ) {
        // 현재 TODO 상태: userId 추출 필요
        Long userId = 1L; // TODO: Long userId = userDetails.getUserId();
        
        BlogPageResponse response = blogService.getMyBlogs(userId, page);
        return ResponseEntity.ok(response);
    }
}
```

### 2. Service Layer

**위치**: `BlogService.java`

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogService {
    
    private final BlogRepository blogRepository;
    private static final int PAGE_SIZE = 4;

    public BlogPageResponse getMyBlogs(Long userId, int page) {
        // 1-based를 0-based로 변환
        int pageIndex = Math.max(0, page - 1);
        
        // 페이지네이션 생성
        Pageable pageable = PageRequest.of(pageIndex, PAGE_SIZE);
        
        // userId로 블로그 조회 (최신순 정렬)
        Page<Blog> blogPage = blogRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        // Blog -> BlogResponse 변환
        Page<BlogResponse> responsePage = blogPage.map(BlogResponse::from);
        
        // BlogPageResponse로 래핑
        return BlogPageResponse.from(responsePage);
    }
}
```

### 3. Repository Layer

**위치**: `BlogRepository.java`

```java
public interface BlogRepository extends JpaRepository<Blog, Long> {
    
    // userId로 블로그 조회 (생성일 기준 내림차순)
    Page<Blog> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
```

### 4. Response DTOs

**BlogResponse.java**
```java
public record BlogResponse(
        Long id,
        Long blogTemplateId,
        String title,
        String imgUrl,
        String content,
        String category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean isToday
) {
    public static BlogResponse from(Blog blog) {
        return new BlogResponse(
                blog.getId(),
                blog.getBlogTemplateId(),
                blog.getTitle(),
                blog.getImgUrl(),
                blog.getContent(),
                blog.getCategory(),
                blog.getCreatedAt(),
                blog.getUpdatedAt(),
                blog.getCreatedAt().toLocalDate().equals(LocalDate.now())
        );
    }
}
```

**BlogPageResponse.java**
```java
public record BlogPageResponse(
        List<BlogResponse> blogs,
        int currentPage,
        int totalPages,
        long totalElements,
        int size,
        boolean isFirst,
        boolean isLast
) {
    public static BlogPageResponse from(Page<BlogResponse> page) {
        return new BlogPageResponse(
                page.getContent(),
                page.getNumber() + 1,  // 0-based를 1-based로 변환
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
    }
}
```

---

## JWT 토큰 처리 흐름

### 1. HTTP 헤더에서 토큰 추출

**JwtAuthenticationFilter.java:47-48**
```java
String authHeader = request.getHeader("Authorization");
// 예: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

String token = authHeader.substring(7); // "Bearer " 제거 (7자)
// 결과: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

이 부분은 단순히 HTTP 헤더에서 `"Bearer "` 접두사를 제거하여 **순수 JWT 토큰 문자열**을 추출합니다.

### 2. SecurityContext에 Principal 저장

**JwtAuthenticationFilter.java:53-57**
```java
Long userId = jwtUtil.getUserId(token);

UsernamePasswordAuthenticationToken authenticationToken =
    new UsernamePasswordAuthenticationToken(
        userId.toString(),  // ← Long을 String으로 변환
        null,
        Collections.emptyList()
    );

SecurityContextHolder.getContext().setAuthentication(authenticationToken);
```

여기서 `userId.toString()`으로 **Long → String 변환**하여 Principal로 저장합니다.

### 3. 문제점

- SecurityContext에는 **String 타입**(`"1"`, `"2"` 같은 문자열)이 저장됨
- Controller에서 `@AuthenticationPrincipal UserDetails`로 받을 수 없음
- UserDetails 인터페이스에는 `getUserId()` 메서드가 없음

---

## Spring Security Principal 처리 방식 비교

### 1. UserDetails 구현체 사용 (표준, 권장) ✅

```java
// CustomUserDetails 구현
public class CustomUserDetails implements UserDetails {
    private final Long userId;
    private final String email;
    
    public Long getUserId() {
        return userId;
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    // ... 기타 메서드
}

// Filter에서 설정
UsernamePasswordAuthenticationToken authenticationToken =
    new UsernamePasswordAuthenticationToken(
        new CustomUserDetails(user),  // UserDetails 구현체
        null,
        customUserDetails.getAuthorities()
    );

// Controller에서 사용
@GetMapping("/my")
public ResponseEntity<?> getMyData(
    @AuthenticationPrincipal CustomUserDetails userDetails
) {
    Long userId = userDetails.getUserId();
    // ...
}
```

### 2. String 사용 (현재 코드베이스 방식)

```java
// Filter
UsernamePasswordAuthenticationToken authenticationToken =
    new UsernamePasswordAuthenticationToken(
        userId.toString(),  // String
        null,
        Collections.emptyList()
    );

// Controller
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
Long userId = Long.parseLong((String) auth.getPrincipal());
```

**단점**: 타입 안정성 없음, 형변환 필요, 추가 정보 저장 불가

### 3. Long 직접 사용

```java
// Filter
UsernamePasswordAuthenticationToken authenticationToken =
    new UsernamePasswordAuthenticationToken(
        userId,  // Long 객체
        null,
        Collections.emptyList()
    );

// Controller
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
Long userId = (Long) auth.getPrincipal();
```

**단점**: UserDetails 인터페이스의 이점을 활용하지 못함

### 비교표

| 방식 | 타입 안정성 | 추가 정보 | 권한 관리 | Spring 표준 | 추천도 |
|------|------------|----------|----------|-------------|--------|
| **UserDetails 구현체** | ✅ 우수 | ✅ 가능 | ✅ 표준 | ✅ 예 | ⭐⭐⭐⭐⭐ |
| String | ❌ 약함 | ❌ 불가 | ⚠️ 어려움 | ❌ 아니오 | ⭐ |
| Long | ⚠️ 보통 | ❌ 불가 | ⚠️ 어려움 | ❌ 아니오 | ⭐⭐ |

---

## TODO 해결 방법

현재 `BlogController.java:52`의 TODO를 해결하는 두 가지 방법:

### 방법 1: SecurityContext에서 직접 userId 추출 (간단, 빠른 적용)

```java
@GetMapping("/my")
@Operation(summary = "내 블로그 글 조회")
public ResponseEntity<BlogPageResponse> getMyBlogs(
        @RequestParam(defaultValue = "1") int page
) {
    // SecurityContext에서 userId 추출 (String으로 저장되어 있음)
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Long userId = Long.parseLong((String) authentication.getPrincipal());
    
    BlogPageResponse response = blogService.getMyBlogs(userId, page);
    return ResponseEntity.ok(response);
}
```

**장점**:
- 기존 코드 최소 변경
- 빠르게 적용 가능

**단점**:
- 타입 안정성 부족
- 매번 형변환 필요
- 코드 중복 발생 가능

### 방법 2: CustomUserDetails 구현 (권장, 확장성 좋음)

이 방법은 [다음 섹션](#모범-사례-customuserdetails-구현)에서 자세히 설명합니다.

---

## 모범 사례: CustomUserDetails 구현

### 1. CustomUserDetails 클래스 생성

**위치**: `src/main/java/io/github/cryschan/berepository/_global/security/CustomUserDetails.java`

```java
package io.github.cryschan.berepository._global.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    
    private final Long userId;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }
    
    @Override
    public String getPassword() {
        return null;
    }
    
    @Override
    public String getUsername() {
        return userId.toString();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

### 2. JwtAuthenticationFilter 수정

**위치**: `JwtAuthenticationFilter.java:53-57`

**수정 전**:
```java
Long userId = jwtUtil.getUserId(token);

UsernamePasswordAuthenticationToken authenticationToken =
    new UsernamePasswordAuthenticationToken(
        userId.toString(),  // String 사용
        null,
        Collections.emptyList()
    );
```

**수정 후**:
```java
Long userId = jwtUtil.getUserId(token);

CustomUserDetails userDetails = new CustomUserDetails(userId);

UsernamePasswordAuthenticationToken authenticationToken =
    new UsernamePasswordAuthenticationToken(
        userDetails,  // CustomUserDetails 사용
        null,
        userDetails.getAuthorities()
    );
```

### 3. Controller에서 사용

**위치**: `BlogController.java:47-57`

**수정 전**:
```java
@GetMapping("/my")
public ResponseEntity<BlogPageResponse> getMyBlogs(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam(defaultValue = "1") int page
) {
    Long userId = 1L; // TODO: Long userId = userDetails.getUserId();
    
    BlogPageResponse response = blogService.getMyBlogs(userId, page);
    return ResponseEntity.ok(response);
}
```

**수정 후**:
```java
@GetMapping("/my")
public ResponseEntity<BlogPageResponse> getMyBlogs(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(defaultValue = "1") int page
) {
    Long userId = userDetails.getUserId();
    
    BlogPageResponse response = blogService.getMyBlogs(userId, page);
    return ResponseEntity.ok(response);
}
```

### 4. 장점

1. **타입 안정성**: 컴파일 타임에 타입 체크
2. **코드 명확성**: `userDetails.getUserId()`로 명확한 의도 표현
3. **확장 가능성**: 이메일, 역할 등 추가 정보 저장 가능
4. **Spring 표준**: Spring Security의 모범 사례
5. **재사용성**: 다른 Controller에서도 동일하게 사용

### 5. 확장 예시 (추가 정보 포함)

```java
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    
    private final Long userId;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    // ... 기타 메서드
}

// Filter에서 사용
User user = userRepository.findById(userId).orElseThrow();
List<GrantedAuthority> authorities = user.getRoles().stream()
    .map(role -> new SimpleGrantedAuthority(role))
    .collect(Collectors.toList());

CustomUserDetails userDetails = new CustomUserDetails(
    user.getUserId(),
    user.getEmail(),
    authorities
);

// Controller에서 사용
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/blogs")
public ResponseEntity<?> getAdminBlogs(
    @AuthenticationPrincipal CustomUserDetails userDetails
) {
    Long userId = userDetails.getUserId();
    String email = userDetails.getEmail();
    // ...
}
```

---

## API 사용 예시

### 요청

```http
GET /api/blogs/my?page=1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 응답

```json
{
  "blogs": [
    {
      "id": 1,
      "blogTemplateId": 1,
      "title": "오늘의 패션",
      "imgUrl": "https://example.com/image.jpg",
      "content": "블로그 내용...",
      "category": "FASHION",
      "createdAt": "2025-11-23T10:00:00",
      "updatedAt": "2025-11-23T10:00:00",
      "isToday": true
    }
  ],
  "currentPage": 1,
  "totalPages": 5,
  "totalElements": 20,
  "size": 4,
  "isFirst": true,
  "isLast": false
}
```

---

## 결론

**Spring Security의 표준은 UserDetails 구현체를 사용하는 것**입니다.

- Long이나 String을 직접 사용하는 것은 간단한 프로토타입이나 학습 목적에서는 가능
- 실무 프로젝트에서는 **CustomUserDetails 구현체를 만들어 사용하는 것이 모범 사례**
- 타입 안정성, 확장성, 유지보수성 측면에서 모두 우수

### 권장 사항

현재 코드베이스를 개선하려면:
1. **CustomUserDetails 구현체 생성**
2. **JwtAuthenticationFilter 수정** (String 대신 CustomUserDetails 사용)
3. **모든 Controller에서 @AuthenticationPrincipal CustomUserDetails 사용**

이렇게 하면 일관성 있고 유지보수하기 쉬운 코드를 작성할 수 있습니다.
