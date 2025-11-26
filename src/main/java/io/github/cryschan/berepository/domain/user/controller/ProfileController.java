package io.github.cryschan.berepository.domain.user.controller;

import io.github.cryschan.berepository.domain.user.dto.request.UpdateProfileRequest;
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

/**
 * 사용자 프로필 관련 컨트롤러
 * 마이페이지 조회, 수정 등 프로필 관리 기능을 처리합니다.
 */
@Slf4j
@Tag(name = "사용자 프로필", description = "사용자 프로필 조회 및 관리 API")
@RequiredArgsConstructor
@RequestMapping("/api/user-profile")
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

    @Operation(
            summary = "다른 사용자 프로필 조회",
            description = "특정 사용자의 프로필 정보를 조회합니다. JWT 토큰이 필요합니다.",
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
                                                "userId": 2,
                                                "email": "other@repository.com",
                                                "username": "김철수",
                                                "department": "기획팀",
                                                "role": "USER",
                                                "tokenUsage": 2500,
                                                "createdAt": "2024-10-15T09:00:00",
                                                "updatedAt": "2024-11-20T11:15:00"
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
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "접근 권한이 없습니다",
                                                "status": 403,
                                                "code": "U005",
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
                                                "message": "사용자를 찾을 수 없습니다. ID: 2",
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
    @GetMapping("/{targetUserId}")
    public UserDetailResponse userProfile(@AuthenticationPrincipal Long userId, @PathVariable Long targetUserId) {
        return profileService.getUserProfile(userId, targetUserId);
    }

    @Operation(
            summary = "프로필 업데이트",
            description = "인증된 사용자의 프로필 정보를 업데이트합니다. JWT 토큰이 필요합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "프로필 업데이트 성공",
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
                                                "updatedAt": "2024-11-25T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효성 검증 실패)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "이름은 필수입니다, 부서명은 필수입니다",
                                                "status": 400,
                                                "code": "C001",
                                                "timestamp": "2024-11-25T10:30:45.123456",
                                                "errors": [
                                                    {
                                                        "field": "username",
                                                        "message": "이름은 필수입니다"
                                                    },
                                                    {
                                                        "field": "department",
                                                        "message": "부서명은 필수입니다"
                                                    }
                                                ]
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
                                                "timestamp": "2024-11-25T10:30:45.123456",
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
                                                "timestamp": "2024-11-25T10:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            )
    })
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/update")
    public UserDetailResponse updateProfile(@AuthenticationPrincipal Long userId, @RequestBody UpdateProfileRequest updateProfileRequest) {
        return profileService.updateMyProfile(userId, updateProfileRequest);
    }
}
