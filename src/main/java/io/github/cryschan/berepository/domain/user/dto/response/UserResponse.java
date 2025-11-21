package io.github.cryschan.berepository.domain.user.dto.response;

import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 */
@Builder
public record UserResponse(
        String email,
        String username,
        LocalDateTime createdAt,
        UserRole role
) {

    /**
     * User 엔티티를 UserResponse DTO로 변환
     *
     * @param user User 엔티티
     * @return UserResponse DTO
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}