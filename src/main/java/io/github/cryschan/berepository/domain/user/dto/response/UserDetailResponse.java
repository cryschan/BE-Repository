package io.github.cryschan.berepository.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 마이페이지 조회용 응답 DTO
 * - 사용자의 상세 정보를 반환
 * - 프로필 정보 수정 화면에 필요한 데이터 제공
 */
public record UserDetailResponse(
        @Schema(description = "사용자 고유 ID", example = "1")
        Long userId,

        @Schema(description = "이메일 주소", example = "user@example.com")
        String email,

        @Schema(description = "사용자 이름", example = "홍길동")
        String username,

        @Schema(description = "부서명", example = "개발팀")
        String department,

        @Schema(description = "사용자 권한", example = "USER")
        UserRole role,

        @Schema(description = "토큰 사용량", example = "1000")
        Long tokenUsage,

        @Schema(description = "계정 생성일시", example = "2024-01-01T10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @Schema(description = "최종 수정일시", example = "2024-01-15T14:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {
    /**
     * User 엔티티를 UserDetailResponse로 변환
     *
     * @param user User 엔티티
     * @return UserDetailResponse DTO
     */
    public static UserDetailResponse from(User user) {
        return new UserDetailResponse(
                user.getUserId(),
                user.getEmail(),
                user.getUsername(),
                user.getDepartment(),
                user.getRole(),
                user.getTokenUsage(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
