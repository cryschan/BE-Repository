# JWT (JSON Web Token) 베스트 프랙티스 가이드

## 📌 목차
1. [JWT 개요](#jwt-개요)
2. [JWT 구조와 동작 원리](#jwt-구조와-동작-원리)
3. [보안 베스트 프랙티스](#보안-베스트-프랙티스)
4. [토큰 관리 전략](#토큰-관리-전략)
5. [Spring Boot에서의 구현](#spring-boot에서의-구현)
6. [일반적인 보안 취약점과 대응](#일반적인-보안-취약점과-대응)
7. [모니터링과 로깅](#모니터링과-로깅)

---

## JWT 개요

JWT는 당사자 간에 JSON 객체로 안전하게 정보를 전송하기 위한 컴팩트하고 독립적인 방법입니다. 디지털 서명이 되어 있어 정보의 검증과 신뢰가 가능합니다.

### 주요 사용 사례
- **인증(Authentication)**: 사용자 로그인 후 각 요청에 JWT 포함
- **정보 교환**: 서명된 토큰으로 안전한 정보 전송
- **권한 부여(Authorization)**: 사용자 권한 정보 포함

---

## JWT 구조와 동작 원리

### JWT 구조
JWT는 점(.)으로 구분된 3개 부분으로 구성:

```
xxxxx.yyyyy.zzzzz
Header.Payload.Signature
```

#### 1. Header
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

#### 2. Payload
```json
{
  "sub": "user123",
  "name": "John Doe",
  "iat": 1516239022,
  "exp": 1516242622,
  "roles": ["USER", "ADMIN"]
}
```

#### 3. Signature
```javascript
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret
)
```

---

## 보안 베스트 프랙티스

### 1. 🔐 강력한 비밀키 사용

```yaml
# application.yml
jwt:
  secret: ${JWT_SECRET:#{T(java.util.UUID).randomUUID().toString()}}
  expiration: 3600000 # 1시간
  refresh-expiration: 604800000 # 7일
```

**권장사항:**
- 최소 256비트 이상의 비밀키 사용
- 환경변수로 관리
- 정기적인 키 로테이션

### 2. 🕐 적절한 만료 시간 설정

```java
public class JwtTokenProvider {
    private static final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000; // 15분
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7일
    
    public String createAccessToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY);
        
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
}
```

### 3. 🔄 Refresh Token 전략

```java
@Service
public class AuthService {
    
    public TokenResponse refreshToken(String refreshToken) {
        // 1. Refresh Token 검증
        if (!validateRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        
        // 2. 새로운 Access Token 발급
        String newAccessToken = generateAccessToken(getUserFromToken(refreshToken));
        
        // 3. Refresh Token Rotation (선택적)
        String newRefreshToken = generateRefreshToken(getUserFromToken(refreshToken));
        
        return TokenResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .build();
    }
}
```

### 4. 🛡️ HTTPS 전용 사용

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .requiresChannel()
            .anyRequest()
            .requiresSecure() // HTTPS 강제
            .and()
            .csrf().disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            
        return http.build();
    }
}
```

### 5. 🚫 민감한 정보 포함 금지

```java
// ❌ 잘못된 예시
Claims claims = Jwts.claims();
claims.put("password", user.getPassword());
claims.put("creditCard", user.getCreditCardNumber());

// ✅ 올바른 예시
Claims claims = Jwts.claims();
claims.put("userId", user.getId());
claims.put("username", user.getUsername());
claims.put("roles", user.getRoles());
```

---

## 토큰 관리 전략

### 1. 토큰 저장 위치

#### 🍪 HttpOnly Cookie (권장)
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
    // 인증 처리
    String token = authService.authenticate(request);
    
    // HttpOnly Cookie 설정
    Cookie cookie = new Cookie("jwt", token);
    cookie.setHttpOnly(true);  // XSS 방지
    cookie.setSecure(true);    // HTTPS 전용
    cookie.setPath("/");
    cookie.setMaxAge(3600);    // 1시간
    cookie.setSameSite("Strict"); // CSRF 방지
    
    response.addCookie(cookie);
    return ResponseEntity.ok().build();
}
```

#### 📦 LocalStorage/SessionStorage (주의 필요)
```javascript
// XSS 취약점 있음 - 주의해서 사용
// localStorage.setItem('token', jwt);

// 더 나은 방법: 메모리에 저장
class TokenManager {
    constructor() {
        this.token = null;
    }
    
    setToken(token) {
        this.token = token;
    }
    
    getToken() {
        return this.token;
    }
    
    clearToken() {
        this.token = null;
    }
}
```

### 2. Token Blacklist 구현

```java
@Service
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public void blacklistToken(String token, long expirationTime) {
        String jti = getJtiFromToken(token);
        redisTemplate.opsForValue().set(
            "blacklist:" + jti, 
            "true", 
            expirationTime, 
            TimeUnit.MILLISECONDS
        );
    }
    
    public boolean isBlacklisted(String token) {
        String jti = getJtiFromToken(token);
        return Boolean.TRUE.equals(
            redisTemplate.hasKey("blacklist:" + jti)
        );
    }
}
```

### 3. Token Rotation

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain chain) {
        String token = extractToken(request);
        
        if (token != null && tokenProvider.validateToken(token)) {
            // 토큰이 곧 만료될 경우 새 토큰 발급
            if (tokenProvider.shouldRefresh(token)) {
                String newToken = tokenProvider.refreshToken(token);
                response.setHeader("X-New-Token", newToken);
            }
            
            // 인증 설정
            Authentication auth = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        chain.doFilter(request, response);
    }
}
```

---

## Spring Boot에서의 구현

### 1. 의존성 추가

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

### 2. JWT 유틸리티 클래스

```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }
    
    public String createToken(String username, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);
        claims.put("jti", UUID.randomUUID().toString()); // Token ID
        
        Date now = new Date();
        Date validity = new Date(now.getTime() + 3600000);
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token);
            
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public String getUsername(String token) {
        return Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
}
```

### 3. Security Configuration

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and()
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
            );
            
        return http.build();
    }
}
```

---

## 일반적인 보안 취약점과 대응

### 1. 🎯 XSS (Cross-Site Scripting) 공격

**문제점**: LocalStorage에 저장된 JWT가 XSS 공격으로 탈취 가능

**대응방안**:
```java
// HttpOnly Cookie 사용
Cookie cookie = new Cookie("jwt", token);
cookie.setHttpOnly(true);  // JavaScript 접근 불가
cookie.setSecure(true);    // HTTPS만
cookie.setSameSite("Strict");
```

### 2. 🔄 CSRF (Cross-Site Request Forgery) 공격

**대응방안**:
```java
// Double Submit Cookie Pattern
@PostMapping("/api/protected")
public ResponseEntity<?> protectedEndpoint(
    @RequestHeader("X-CSRF-TOKEN") String csrfToken,
    @CookieValue("CSRF-TOKEN") String cookieCsrfToken) {
    
    if (!csrfToken.equals(cookieCsrfToken)) {
        throw new InvalidCsrfTokenException();
    }
    
    // 처리 로직
}
```

### 3. 🕵️ Token Replay Attack

**대응방안**:
```java
public class JwtTokenProvider {
    
    public String createToken(String username) {
        String jti = UUID.randomUUID().toString();
        String fingerprint = generateFingerprint();
        
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("jti", jti);
        claims.put("fingerprint", hashFingerprint(fingerprint));
        
        // Token에 고유 ID와 fingerprint 포함
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
}
```

### 4. 💣 Algorithm Confusion Attack

**대응방안**:
```java
// 알고리즘 명시적 지정
public boolean validateToken(String token) {
    try {
        Jwts.parser()
            .setSigningKey(secretKey)
            .requireIssuer("your-app")
            .require("typ", "JWT")
            .parseClaimsJws(token);
        return true;
    } catch (JwtException e) {
        return false;
    }
}
```

---

## 모니터링과 로깅

### 1. 보안 이벤트 로깅

```java
@Component
@Slf4j
public class SecurityEventLogger {
    
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        String ip = getClientIP();
        log.info("Successful login - User: {}, IP: {}", username, ip);
    }
    
    @EventListener
    public void handleAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String ip = getClientIP();
        log.warn("Failed login attempt - User: {}, IP: {}, Reason: {}", 
                username, ip, event.getException().getMessage());
    }
    
    @EventListener
    public void handleTokenBlacklist(TokenBlacklistEvent event) {
        log.info("Token blacklisted - JTI: {}, User: {}", 
                event.getJti(), event.getUsername());
    }
}
```

### 2. 메트릭 수집

```java
@Component
@RequiredArgsConstructor
public class JwtMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public void recordTokenGeneration() {
        meterRegistry.counter("jwt.token.generated").increment();
    }
    
    public void recordTokenValidation(boolean success) {
        meterRegistry.counter("jwt.token.validation",
            "result", success ? "success" : "failure").increment();
    }
    
    public void recordTokenExpiration() {
        meterRegistry.counter("jwt.token.expired").increment();
    }
}
```

### 3. 감사 로그

```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String action;
    private String resource;
    private String ipAddress;
    private LocalDateTime timestamp;
    private boolean success;
    private String failureReason;
    
    // getters and setters
}
```

---

## 체크리스트

### 개발 시 확인사항

- [ ] **비밀키 관리**
  - [ ] 256비트 이상의 강력한 키 사용
  - [ ] 환경변수로 관리
  - [ ] 키 로테이션 계획 수립

- [ ] **토큰 설정**
  - [ ] 적절한 만료 시간 설정 (Access: 15-30분, Refresh: 7-14일)
  - [ ] 민감한 정보 미포함 확인
  - [ ] JTI (JWT ID) 포함

- [ ] **보안 설정**
  - [ ] HTTPS 강제
  - [ ] HttpOnly Cookie 사용
  - [ ] SameSite 속성 설정
  - [ ] CORS 적절히 설정

- [ ] **에러 처리**
  - [ ] 토큰 만료 처리
  - [ ] 유효하지 않은 토큰 처리
  - [ ] Refresh Token 실패 처리

- [ ] **모니터링**
  - [ ] 로그인 성공/실패 로깅
  - [ ] 토큰 발급/검증 메트릭
  - [ ] 비정상적인 패턴 감지

---

## 참고 자료

- [RFC 7519 - JSON Web Token (JWT)](https://tools.ietf.org/html/rfc7519)
- [OWASP JWT Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [Spring Security JWT Tutorial](https://spring.io/guides/tutorials/spring-security-and-angular-js/)
- [JWT.io - JWT Debugger](https://jwt.io/)

---

## 마치며

JWT는 강력한 인증 메커니즘이지만, 올바르게 구현하지 않으면 보안 취약점이 될 수 있습니다. 위의 베스트 프랙티스를 따라 안전하고 확장 가능한 인증 시스템을 구축하시기 바랍니다.

정기적인 보안 감사와 최신 보안 동향 파악을 통해 시스템을 지속적으로 개선해 나가는 것이 중요합니다.