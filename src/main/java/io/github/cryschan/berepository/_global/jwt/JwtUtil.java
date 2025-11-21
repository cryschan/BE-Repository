package io.github.cryschan.berepository._global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 유틸리티 클래스입니다.
 * <p>
 * JJWT 라이브러리를 사용하여 JWT 액세스 토큰을 생성하고 검증합니다.
 * 토큰에는 사용자 ID와 이메일 정보가 포함되며, 만료 시간이 설정됩니다.
 * </p>
 *
 * @author tato126
 * @since 1.0
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final Long accessTokenExpiration;

    /**
     * JwtUtil을 초기화합니다.
     *
     * @param secret                 JWT 서명에 사용할 비밀키 (application.yml에서 주입)
     * @param accessTokenExpiration  액세스 토큰 만료 시간 (밀리초)
     */
    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.access-token-expiration}") long accessTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
    }

    /**
     * JWT 액세스 토큰을 생성합니다.
     * <p>
     * 토큰에는 사용자 ID와 이메일이 포함되며, 설정된 만료 시간이 적용됩니다.
     * </p>
     *
     * @param userId 사용자 ID
     * @return 생성된 JWT 토큰 문자열
     */
    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT 토큰을 검증하고 Claims를 추출합니다.
     * <p>
     * 토큰의 서명을 검증하고, 만료 여부를 확인합니다.
     * </p>
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰의 Claims 정보
     * @throws io.jsonwebtoken.JwtException 토큰이 유효하지 않거나 만료된 경우
     */
    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * JWT 토큰에서 사용자 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Long getUserId(String token) {
        Claims claims = validateToken(token);
        return claims.get("userId", Long.class);
    }
}
