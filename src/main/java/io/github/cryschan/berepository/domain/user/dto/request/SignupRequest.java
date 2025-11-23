package io.github.cryschan.berepository.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 DTO
 */
public record SignupRequest(
        @Schema(description = "사용자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "사용자 이름은 필수입니다")
        String username,

        @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email,

        @Schema(description = "부서명", example = "개발팀")
        String department,

        @Schema(description = "비밀번호 (8-20자)", example = "password123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다")
        String password
) {
}
