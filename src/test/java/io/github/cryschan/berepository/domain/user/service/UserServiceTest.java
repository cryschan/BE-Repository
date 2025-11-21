package io.github.cryschan.berepository.domain.user.service;

import io.github.cryschan.berepository._global.jwt.JwtUtil;
import io.github.cryschan.berepository.domain.user.dto.request.LoginRequest;
import io.github.cryschan.berepository.domain.user.dto.response.UserResponse;
import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import io.github.cryschan.berepository.domain.user.exception.DuplicationUserException;
import io.github.cryschan.berepository.domain.user.exception.InvalidCredentialsException;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private LoginRequest loginRequest;
    private User user;
    private User savedUser;  // DB에 저장된 후 ID가 할당된 User
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        loginRequest = new LoginRequest(
                "테스트유저",        // username
                "test@example.com",  // email
                "개발팀",           // department
                "password123"        // password
        );

        encodedPassword = "encoded_password123";

        // 저장 전 User (ID 없음)
        user = User.builder()
                .email("test@example.com")
                .password(encodedPassword)
                .username("테스트유저")
                .department("개발팀")
                .role(UserRole.USER)
                .build();

        // 저장 후 User (ID 있음) - Mock 객체 생성
        savedUser = Mockito.mock(User.class, Mockito.RETURNS_SMART_NULLS);

        // Mock 설정 (lenient 설정으로 필요한 경우에만 사용되도록)
        lenient().when(savedUser.getUserId()).thenReturn(1L);
        lenient().when(savedUser.getEmail()).thenReturn("test@example.com");
        lenient().when(savedUser.getUsername()).thenReturn("테스트유저");
        lenient().when(savedUser.getDepartment()).thenReturn("개발팀");
        lenient().when(savedUser.getRole()).thenReturn(UserRole.USER);
        lenient().when(savedUser.getPassword()).thenReturn(encodedPassword);
        lenient().when(savedUser.getCreatedAt()).thenReturn(null);
        lenient().when(savedUser.getUpdatedAt()).thenReturn(null);
        lenient().when(savedUser.getTokenUsage()).thenReturn(null);
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class SignupTest {

        @Test
        @DisplayName("성공: 정상적인 회원가입 요청시 유저가 생성되고 응답이 반환된다")
        void signup_Success() {
            // given
            given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
            given(passwordEncoder.encode(anyString())).willReturn(encodedPassword);
            given(userRepository.save(any(User.class))).willReturn(savedUser);  // ID가 있는 User 반환

            // when
            UserResponse response = userService.signup(loginRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.userId()).isEqualTo(1L);  // userId 검증 추가
            assertThat(response.email()).isEqualTo("test@example.com");
            assertThat(response.username()).isEqualTo("테스트유저");
            assertThat(response.role()).isEqualTo(UserRole.USER);

            // 검증: 메서드 호출 확인
            verify(userRepository).findByEmail("test@example.com");
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("실패: 이미 존재하는 이메일로 회원가입 시도시 DuplicationUserException이 발생한다")
        void signup_Fail_DuplicateEmail() {
            // given
            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(savedUser));

            // when & then
            assertThatThrownBy(() -> userService.signup(loginRequest))
                    .isInstanceOf(DuplicationUserException.class)
                    .hasMessageContaining("test@example.com");

            // 검증: save 메서드가 호출되지 않았는지 확인
            verify(userRepository).findByEmail("test@example.com");
            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("성공: 올바른 이메일과 비밀번호로 로그인시 JWT 토큰과 함께 유저 정보가 반환된다")
        void login_Success_With_JWT() {
            // given
            String expectedToken = "jwt.token.here";
            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(savedUser));  // ID가 있는 User 반환
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
            given(jwtUtil.generateAccessToken(anyLong())).willReturn(expectedToken);

            // when
            UserResponse response = userService.login(loginRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.userId()).isEqualTo(1L);  // userId 검증 추가
            assertThat(response.email()).isEqualTo("test@example.com");
            assertThat(response.username()).isEqualTo("테스트유저");
            assertThat(response.role()).isEqualTo(UserRole.USER);
            assertThat(response.token()).isEqualTo(expectedToken);  // JWT 토큰 검증

            // 검증
            verify(userRepository).findByEmail("test@example.com");
            verify(passwordEncoder).matches("password123", encodedPassword);
            verify(jwtUtil).generateAccessToken(1L);  // JWT 토큰 생성 메서드 호출 확인
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이메일로 로그인시 InvalidCredentialsException이 발생한다")
        void login_Fail_UserNotFound() {
            // given
            given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.login(loginRequest))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("test@example.com");

            // 검증
            verify(userRepository).findByEmail("test@example.com");
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("실패: 잘못된 비밀번호로 로그인시 InvalidCredentialsException이 발생한다")
        void login_Fail_InvalidPassword() {
            // given
            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(savedUser));  // ID가 있는 User 사용
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.login(loginRequest))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("test@example.com");

            // 검증
            verify(userRepository).findByEmail("test@example.com");
            verify(passwordEncoder).matches("password123", encodedPassword);
        }

        @Test
        @DisplayName("엣지케이스: null 비밀번호로 로그인 시도시 InvalidCredentialsException이 발생한다")
        void login_EdgeCase_NullPassword() {
            // given
            LoginRequest invalidRequest = new LoginRequest(
                    "테스트유저",
                    "test@example.com",
                    "개발팀",
                    null  // null password
            );
            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(savedUser));
            given(passwordEncoder.matches(any(), anyString())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.login(invalidRequest))
                    .isInstanceOf(InvalidCredentialsException.class);

            // 검증
            verify(userRepository).findByEmail("test@example.com");
            verify(passwordEncoder).matches(null, encodedPassword);
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("시나리오: 회원가입 후 로그인 플로우")
        void signupAndLogin_Scenario() {
            // 1. 회원가입 시뮬레이션
            given(userRepository.findByEmail(anyString()))
                    .willReturn(Optional.empty())  // 첫 번째 호출: 회원가입시 중복 체크
                    .willReturn(Optional.of(savedUser)); // 두 번째 호출: 로그인시 유저 조회 (ID가 있는 User)
            given(passwordEncoder.encode(anyString())).willReturn(encodedPassword);
            given(userRepository.save(any(User.class))).willReturn(savedUser);  // ID가 있는 User 반환
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            // 회원가입
            UserResponse signupResponse = userService.signup(loginRequest);
            assertThat(signupResponse).isNotNull();
            assertThat(signupResponse.userId()).isEqualTo(1L);  // userId 검증 추가
            assertThat(signupResponse.email()).isEqualTo("test@example.com");

            // 로그인
            UserResponse loginResponse = userService.login(loginRequest);
            assertThat(loginResponse).isNotNull();
            assertThat(loginResponse.userId()).isEqualTo(1L);  // userId 검증 추가
            assertThat(loginResponse.email()).isEqualTo("test@example.com");

            // 검증
            verify(userRepository, times(2)).findByEmail("test@example.com");
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).matches("password123", encodedPassword);
        }

        @Test
        @DisplayName("시나리오: 중복 회원가입 방지")
        void preventDuplicateSignup_Scenario() {
            // given
            given(userRepository.findByEmail(anyString()))
                    .willReturn(Optional.empty())  // 첫 번째 회원가입: 성공
                    .willReturn(Optional.of(savedUser)); // 두 번째 회원가입: 중복 (ID가 있는 User)
            given(passwordEncoder.encode(anyString())).willReturn(encodedPassword);
            given(userRepository.save(any(User.class))).willReturn(savedUser);  // ID가 있는 User 반환

            // 첫 번째 회원가입: 성공
            UserResponse firstSignup = userService.signup(loginRequest);
            assertThat(firstSignup).isNotNull();
            assertThat(firstSignup.userId()).isEqualTo(1L);  // userId 검증 추가

            // 두 번째 회원가입: 실패
            assertThatThrownBy(() -> userService.signup(loginRequest))
                    .isInstanceOf(DuplicationUserException.class);

            // 검증
            verify(userRepository, times(2)).findByEmail("test@example.com");
            verify(userRepository, times(1)).save(any(User.class));
        }
    }
}