package io.github.cryschan.berepository.domain.user.controller;

import io.github.cryschan.berepository.domain.user.dto.request.LoginRequest;
import io.github.cryschan.berepository.domain.user.dto.request.SignupRequest;
import io.github.cryschan.berepository.domain.user.dto.response.LoginResponse;
import io.github.cryschan.berepository.domain.user.dto.response.UserResponse;
import io.github.cryschan.berepository.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 컨트롤러
 * 회원가입, 로그인 등 인증 기능을 처리합니다.
 */
@Tag(name = "인증", description = "로그인/회원가입 API")
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class LoginController {

    private final UserService userService;

    /**
     * 회원가입
     */
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public UserResponse signup(@Valid @RequestBody SignupRequest signupRequest) {
        return userService.signup(signupRequest);
    }

    /**
     * 로그인
     */
    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인하고 JWT 토큰을 반환합니다. 반환된 토큰은 Swagger의 Authorize 버튼에 입력하여 사용합니다."
    )
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }
}