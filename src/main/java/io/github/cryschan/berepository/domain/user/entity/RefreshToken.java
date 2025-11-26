package io.github.cryschan.berepository.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Refresh Token 엔티티
 * 사용자의 리프레시 토큰 정보를 저장합니다.
 */
@Getter
@NoArgsConstructor
@Entity
public class RefreshToken {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long refreshTokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    // expiresAt (만료일자)
    private LocalDateTime expiresAt;

    public static RefreshToken create(User user, String token, LocalDateTime expiresAt) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.user = user;
        refreshToken.token = token;
        refreshToken.expiresAt = expiresAt;
        return refreshToken;
    }

    // 만료 확인 메서드
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

}
