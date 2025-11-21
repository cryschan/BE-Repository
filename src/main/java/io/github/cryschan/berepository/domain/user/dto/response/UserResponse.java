package io.github.cryschan.berepository.domain.user.dto.response;

import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 */
public record UserResponse(
        @Schema(description = "사용자 고유 ID", example = "1")
        Long userId,

        @Schema(description = "이메일 주소", example = "user@example.com")
        String email,

        @Schema(description = "사용자 이름", example = "홍길동")
        String username,

        @Schema(description = "계정 생성일시", example = "2024-01-01T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "사용자 권한", example = "USER")
        UserRole role,

        @Schema(description = "JWT 액세스 토큰 (로그인 시에만 포함)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token  // JWT 토큰 (로그인 시에만 포함)
) {

    /**
     * User 엔티티를 UserResponse DTO로 변환
     *
     * @param user User 엔티티
     * @return UserResponse DTO
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getUsername(),
                user.getCreatedAt(),
                user.getRole(),
                null  // 회원가입 시에는 토큰이 없음
        );
    }

    /**
     * User 엔티티와 JWT 토큰으로 UserResponse DTO 생성 (로그인용)
     *
     * @param user  User 엔티티
     * @param token JWT 토큰
     * @return UserResponse DTO with token
     */
    public static UserResponse from(User user, String token) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getUsername(),
                user.getCreatedAt(),
                user.getRole(),
                token
        );
    }
}