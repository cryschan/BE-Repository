package io.github.cryschan.berepository._global.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JwtUtil 단위 테스트
 * JWT 토큰 생성 및 검증 기능을 테스트합니다.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String TEST_SECRET = "test-secret-key-for-jwt-testing-minimum-256-bits-required";
    private static final long ACCESS_TOKEN_EXPIRATION = 900000L;  // 15분
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L;  // 7일

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION);
    }

    @Nested
    @DisplayName("Refresh Token 생성 테스트")
    class GenerateRefreshTokenTest {

        @Test
        @DisplayName("성공: userId로 Refresh Token 생성")
        void generateRefreshToken_Success() {
            // given
            Long userId = 1L;

            // when
            String refreshToken = jwtUtil.generateRefreshToken(userId);

            // then
            assertThat(refreshToken).isNotNull();
            assertThat(refreshToken).isNotEmpty();
            assertThat(jwtUtil.getUserId(refreshToken)).isEqualTo(userId);
        }

        @Test
        @DisplayName("성공: 생성된 Refresh Token은 Refresh Token 타입이다")
        void generateRefreshToken_IsRefreshType() {
            // given
            Long userId = 1L;

            // when
            String refreshToken = jwtUtil.generateRefreshToken(userId);

            // then
            assertThat(jwtUtil.isRefreshToken(refreshToken)).isTrue();
        }

        @Test
        @DisplayName("성공: Access Token과 Refresh Token은 다른 값이다")
        void generateRefreshToken_DifferentFromAccessToken() {
            // given
            Long userId = 1L;

            // when
            String accessToken = jwtUtil.generateAccessToken(userId);
            String refreshToken = jwtUtil.generateRefreshToken(userId);

            // then
            assertThat(accessToken).isNotEqualTo(refreshToken);
        }
    }

    @Nested
    @DisplayName("토큰 타입 검증 테스트")
    class IsRefreshTokenTest {

        @Test
        @DisplayName("성공: Access Token은 Refresh Token이 아니다")
        void isRefreshToken_False_ForAccessToken() {
            // given
            Long userId = 1L;
            String accessToken = jwtUtil.generateAccessToken(userId);

            // when
            boolean result = jwtUtil.isRefreshToken(accessToken);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("성공: Refresh Token은 Refresh Token이다")
        void isRefreshToken_True_ForRefreshToken() {
            // given
            Long userId = 1L;
            String refreshToken = jwtUtil.generateRefreshToken(userId);

            // when
            boolean result = jwtUtil.isRefreshToken(refreshToken);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("실패: 잘못된 토큰은 false 반환")
        void isRefreshToken_False_ForInvalidToken() {
            // given
            String invalidToken = "invalid.token.here";

            // when
            boolean result = jwtUtil.isRefreshToken(invalidToken);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Refresh Token 만료 시간 조회 테스트")
    class GetRefreshTokenExpirationTest {

        @Test
        @DisplayName("성공: Refresh Token 만료 시간 반환")
        void getRefreshTokenExpiration_Success() {
            // when
            long expiration = jwtUtil.getRefreshTokenExpiration();

            // then
            assertThat(expiration).isEqualTo(REFRESH_TOKEN_EXPIRATION);
        }
    }

    @Nested
    @DisplayName("기존 Access Token 기능 테스트")
    class AccessTokenTest {

        @Test
        @DisplayName("성공: Access Token 생성 및 userId 추출")
        void generateAccessToken_Success() {
            // given
            Long userId = 1L;

            // when
            String accessToken = jwtUtil.generateAccessToken(userId);

            // then
            assertThat(accessToken).isNotNull();
            assertThat(jwtUtil.getUserId(accessToken)).isEqualTo(userId);
        }

        @Test
        @DisplayName("성공: Access Token은 Refresh Token이 아니다")
        void accessToken_IsNotRefreshToken() {
            // given
            Long userId = 1L;
            String accessToken = jwtUtil.generateAccessToken(userId);

            // when & then
            assertThat(jwtUtil.isRefreshToken(accessToken)).isFalse();
        }
    }
}
