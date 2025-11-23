package io.github.cryschan.berepository.domain.user.dto.response;

import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO (회원가입용)
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
        UserRole role
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
                user.getRole()
        );
    }
}