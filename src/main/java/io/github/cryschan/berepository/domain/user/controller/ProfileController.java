package io.github.cryschan.berepository.domain.user.controller;

import io.github.cryschan.berepository.domain.user.dto.response.UserDetailResponse;
import io.github.cryschan.berepository.domain.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "사용자 프로필", description = "사용자 프로필 조회 및 관리 API")
@RequiredArgsConstructor
@RequestMapping("/api/userProfile")
@RestController
public class ProfileController {

    private final UserProfileService profileService;

    @Operation(
            summary = "내 프로필 조회",
            description = "인증된 사용자 자신의 프로필 정보를 조회합니다. JWT 토큰이 필요합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "프로필 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDetailResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "userId": 1,
                                                "email": "user@repository.com",
                                                "username": "홍길동",
                                                "department": "개발팀",
                                                "role": "USER",
                                                "tokenUsage": 1500,
                                                "createdAt": "2024-11-01T10:00:00",
                                                "updatedAt": "2024-11-24T14:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요 (JWT 토큰 없음/만료)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "인증이 필요합니다",
                                                "status": 401,
                                                "code": "U004",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "사용자를 찾을 수 없습니다. ID: 1",
                                                "status": 404,
                                                "code": "U001",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            )
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public UserDetailResponse myProfile(@AuthenticationPrincipal Long userId) {
        log.debug("UserId : {}", userId);
        return profileService.getMyProfile(userId);
    }

    // 다른 유저 프로필 조회
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{targetUserId}")
    public UserDetailResponse userProfile(@AuthenticationPrincipal Long userId, @PathVariable Long targetUserId) {
        return profileService.getUserProfile(userId, targetUserId);
    }
}
