package io.github.cryschan.berepository.domain.user.service;

import io.github.cryschan.berepository._global.jwt.JwtUtil;
import io.github.cryschan.berepository.domain.user.dto.request.LoginRequest;
import io.github.cryschan.berepository.domain.user.dto.request.RefreshTokenRequest;
import io.github.cryschan.berepository.domain.user.dto.request.SignupRequest;
import io.github.cryschan.berepository.domain.user.dto.response.LoginResponse;
import io.github.cryschan.berepository.domain.user.dto.response.TokenRefreshResponse;
import io.github.cryschan.berepository.domain.user.dto.response.UserResponse;
import io.github.cryschan.berepository.domain.user.entity.RefreshToken;
import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.exception.TokenException;
import io.github.cryschan.berepository.domain.user.exception.UserException;
import io.github.cryschan.berepository.domain.user.repository.TokenRepository;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 인증 서비스
 * 회원가입, 로그인, 로그아웃, 토큰 갱신 등 인증 관련 비즈니스 로직을 처리합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입
    @Transactional
    public UserResponse signup(SignupRequest signupRequest) {

        // 중복 이메일 체크
        if (userRepository.findByEmail(signupRequest.email()).isPresent()) {
            throw UserException.duplication(signupRequest.email());
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupRequest.password());

        // 유저 생성 (암호화된 비밀번호 전달)
        User newUser = User.form(signupRequest, encodedPassword);

        // 회원 등록 (저장 후 ID가 할당된 User 반환)
        User savedUser = userRepository.save(newUser);

        // entity -> dto (ID가 포함된 savedUser 사용)
        return UserResponse.from(savedUser);
    }

    // 로그인
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {

        // 이메일로 사용자 조회 (보안상 이메일 존재 여부를 노출하지 않음)
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(UserException::invalidCredentials);

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw UserException.invalidCredentials();
        }

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getUserId());

        // refresh 토큰 생성
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        // 기존 refresh token 삭제 (1인 1토큰 정책)
        tokenRepository.findByUser(user)
                .ifPresent(tokenRepository::delete);

        // 만료 시간 계산
        java.time.LocalDateTime expiresAt = java.time.LocalDateTime.now()
                .plusNanos(jwtUtil.getRefreshTokenExpiration() * 1_000_000);

        // refresh token 엔티티 생성 및 저장
        RefreshToken refreshTokenEntity = RefreshToken.create(user, refreshToken, expiresAt);
        tokenRepository.save(refreshTokenEntity);

        // entity -> dto (토큰과 함께 반환)
        return LoginResponse.from(user, accessToken, refreshToken);
    }

    // 토큰 갱신
    @Transactional(readOnly = true)
    public TokenRefreshResponse refresh(RefreshTokenRequest request) {

        log.debug("========= 토큰 갱신 시작 =========");
        // 요청에서 refresh token 추출
        String token = request.refreshToken();

        // 토큰 유효성 및 refresh token 여부 검증
        if (!jwtUtil.validateToken(token) || !jwtUtil.isRefreshToken(token)) {
            throw TokenException.invalid();
        }

        // DB에서 refresh token 조회
        RefreshToken refreshToken = tokenRepository.findByToken(token)
                .orElseThrow(TokenException::notFound);

        // 만료 여부 확인
        if (refreshToken.isExpired()) {
            throw TokenException.expired();
        }

        // 토큰에서 사용자 ID 추출
        Long userId = jwtUtil.getUserId(token);

        // 새로운 access token 생성
        String newAccessToken = jwtUtil.generateAccessToken(userId);

        log.debug("========= 토큰 갱신 완료 =========");
        // 새 access token 반환
        return new TokenRefreshResponse(newAccessToken);
    }

    // 로그아웃
    @Transactional
    public void logout(RefreshTokenRequest request) {
        // 요청에서 refresh token 추출
        String token = request.refreshToken();

        // refresh token 삭제 (직접 DELETE 쿼리 실행)
        tokenRepository.deleteByToken(token);
    }
}
