package io.github.cryschan.berepository.domain.user.controller;

import io.github.cryschan.berepository.domain.user.dto.request.LoginRequest;
import io.github.cryschan.berepository.domain.user.dto.request.RefreshTokenRequest;
import io.github.cryschan.berepository.domain.user.dto.request.SignupRequest;
import io.github.cryschan.berepository.domain.user.dto.response.LoginResponse;
import io.github.cryschan.berepository.domain.user.dto.response.TokenRefreshResponse;
import io.github.cryschan.berepository.domain.user.dto.response.UserResponse;
import io.github.cryschan.berepository.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// TODO: user에서 사용하는 404는 데이터 조회 혹은 파라미터 문제시 에러이다. 페이지가 없는 것이 아니기에 다른 응답으로 사용할지 고민필요.
/**
 * 인증 관련 컨트롤러
 * 회원가입, 로그인 등 인증 기능을 처리합니다.
 */
@Tag(name = "인증", description = "로그인/회원가입 API")
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class LoginController {

    private final UserService userService;

    /**
     * 로그인
     */
    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인하고 JWT 토큰을 반환합니다. 반환된 토큰은 Swagger의 Authorize 버튼에 입력하여 사용합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 입력값",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "입력값이 올바르지 않습니다",
                                                "status": 400,
                                                "code": "C001",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": [
                                                    {
                                                        "field": "email",
                                                        "rejectedValue": "",
                                                        "message": "이메일은 필수입니다"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (잘못된 비밀번호)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "이메일 혹은 비밀번호가 일치하지 않습니다.",
                                                "status": 401,
                                                "code": "U003",
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
                                                "message": "사용자를 찾을 수 없습니다. Email: user@example.com",
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
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }

    /**
     * 회원가입
     */
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 입력값",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "입력값이 올바르지 않습니다",
                                                "status": 400,
                                                "code": "C001",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": [
                                                    {
                                                        "field": "email",
                                                        "rejectedValue": "invalid-email",
                                                        "message": "이메일 형식이 올바르지 않습니다"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이메일 중복",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "이미 존재하는 이메일입니다: user@example.com",
                                                "status": 409,
                                                "code": "U002",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            )
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public UserResponse signup(@Valid @RequestBody SignupRequest signupRequest) {
        return userService.signup(signupRequest);
    }

    /**
     * 토큰 갱신
     */
    @Operation(
            summary = "토큰 갱신",
            description = "Refresh Token으로 새로운 Access Token을 발급받습니다"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenRefreshResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 입력값",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "입력값이 올바르지 않습니다",
                                                "status": 400,
                                                "code": "C001",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": [
                                                    {
                                                        "field": "refreshToken",
                                                        "rejectedValue": "",
                                                        "message": "리프레시 토큰은 필수입니다"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않거나 만료된 토큰",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "유효하지 않은 토큰",
                                            value = """
                                                    {
                                                        "message": "유효하지 않은 토큰입니다",
                                                        "status": 401,
                                                        "code": "T001",
                                                        "timestamp": "2024-11-24T14:30:45.123456",
                                                        "errors": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "만료된 토큰",
                                            value = """
                                                    {
                                                        "message": "만료된 토큰입니다",
                                                        "status": 401,
                                                        "code": "T002",
                                                        "timestamp": "2024-11-24T14:30:45.123456",
                                                        "errors": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "토큰을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "토큰을 찾을 수 없습니다",
                                                "status": 404,
                                                "code": "T003",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            )
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/refresh")
    public TokenRefreshResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return userService.refresh(request);
    }

    /**
     * 로그아웃
     */
    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 DB에서 삭제하여 무효화합니다. 토큰이 이미 삭제되었거나 존재하지 않아도 성공 처리됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "로그아웃 성공 (토큰 삭제 완료)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 입력값",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "입력값이 올바르지 않습니다",
                                                "status": 400,
                                                "code": "C001",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": [
                                                    {
                                                        "field": "refreshToken",
                                                        "rejectedValue": "",
                                                        "message": "리프레시 토큰은 필수입니다"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            )
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        userService.logout(request);
    }
}