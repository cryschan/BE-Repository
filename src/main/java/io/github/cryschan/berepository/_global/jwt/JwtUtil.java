package io.github.cryschan.berepository._global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 유틸리티 클래스입니다.
 * <p>
 * JJWT 라이브러리를 사용하여 JWT 액세스 토큰과 리프레시 토큰을 생성하고 검증합니다.
 * 토큰에는 사용자 ID와 토큰 타입 정보가 포함되며, 만료 시간이 설정됩니다.
 * </p>
 *
 * @author tato126
 * @since 1.0
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration:900000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    private SecretKey key;

    public JwtUtil() {
    }

    public JwtUtil(String secret, long accessTokenExpiration, long refreshTokenExpiration) {
        this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT 액세스 토큰을 생성합니다.
     * <p>
     * 토큰에는 사용자 ID와 토큰 타입(access)이 포함되며, 설정된 만료 시간이 적용됩니다.
     * </p>
     *
     * @param userId 사용자 ID
     * @return 생성된 JWT 액세스 토큰 문자열
     */
    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key)
                .compact();
    }

    /**
     * JWT 리프레시 토큰을 생성합니다.
     * <p>
     * 토큰에는 사용자 ID와 토큰 타입(refresh)이 포함되며, 설정된 만료 시간이 적용됩니다.
     * </p>
     *
     * @param userId 사용자 ID
     * @return 생성된 JWT 리프레시 토큰 문자열
     */
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key)
                .compact();
    }

    /**
     * JWT 토큰의 유효성을 검증합니다.
     *
     * @param token 검증할 JWT 토큰
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * JWT 토큰이 리프레시 토큰인지 확인합니다.
     *
     * @param token JWT 토큰
     * @return 리프레시 토큰이면 true, 그렇지 않으면 false
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * JWT 토큰에서 사용자 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 리프레시 토큰 만료 시간(밀리초)을 반환합니다.
     *
     * @return 리프레시 토큰 만료 시간 (밀리초)
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    /**
     * JWT 토큰에서 Claims를 추출합니다.
     *
     * @param token JWT 토큰
     * @return Claims 정보
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
