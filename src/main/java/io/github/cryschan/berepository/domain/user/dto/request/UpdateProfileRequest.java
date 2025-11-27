package io.github.cryschan.berepository.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 마이페이지 업데이트 요청 DTO
 */
public record UpdateProfileRequest(
        @Schema(description = "사용자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "사용자 이름은 필수입니다")
//        @Size(min = 3, max = 50, message = "사용자 이름은 3자 이상 50자 이하여야 합니다")
        String username,

        @Schema(description = "부서명", example = "개발팀")
//        @Size(max = 50, message = "부서명은 50자 이하여야 합니다")
        String department
) {
}
