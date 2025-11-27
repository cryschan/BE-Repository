package io.github.cryschan.berepository.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 토큰 갱신 및 로그아웃 요청 DTO
 */
public record RefreshTokenRequest(
        @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwidHlwZSI6InJlZnJlc2gifQ.xxxxx", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "리프레시 토큰은 필수입니다")
        String refreshToken
) {
}
