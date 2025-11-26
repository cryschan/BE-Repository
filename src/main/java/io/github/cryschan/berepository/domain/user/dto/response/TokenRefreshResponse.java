package io.github.cryschan.berepository.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 토큰 갱신 응답 DTO
 */
public record TokenRefreshResponse(
        @Schema(description = "새로운 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwidHlwZSI6ImFjY2VzcyJ9.xxxxx")
        String accessToken
) {
}
