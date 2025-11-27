package io.github.cryschan.berepository.domain.user.service;

import io.github.cryschan.berepository.domain.user.dto.request.UpdateProfileRequest;
import io.github.cryschan.berepository.domain.user.dto.response.UserDetailResponse;
import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import io.github.cryschan.berepository.domain.user.exception.UserException;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileService 테스트")
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    private User mockUser;
    private Long userId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        userId = 1L;
        now = LocalDateTime.now();

        // Mock User 객체 생성
        mockUser = mock(User.class);
        lenient().when(mockUser.getUserId()).thenReturn(userId);
        lenient().when(mockUser.getEmail()).thenReturn("test@example.com");
        lenient().when(mockUser.getUsername()).thenReturn("테스트유저");
        lenient().when(mockUser.getDepartment()).thenReturn("개발팀");
        lenient().when(mockUser.getRole()).thenReturn(UserRole.USER);
        lenient().when(mockUser.getTokenUsage()).thenReturn(1000L);
        lenient().when(mockUser.getCreatedAt()).thenReturn(now.minusDays(30));
        lenient().when(mockUser.getUpdatedAt()).thenReturn(now.minusDays(1));
    }

    @Nested
    @DisplayName("내 프로필 조회")
    class GetMyProfile {

        @Test
        @DisplayName("성공: 인증된 사용자가 자신의 프로필을 조회한다")
        void getMyProfile_Success() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

            // when
            UserDetailResponse response = userProfileService.getMyProfile(userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.email()).isEqualTo("test@example.com");
            assertThat(response.username()).isEqualTo("테스트유저");
            assertThat(response.department()).isEqualTo("개발팀");
            assertThat(response.role()).isEqualTo(UserRole.USER);
            assertThat(response.tokenUsage()).isEqualTo(1000L);
            assertThat(response.createdAt()).isEqualTo(now.minusDays(30));
            assertThat(response.updatedAt()).isEqualTo(now.minusDays(1));

            // 검증
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자 ID로 조회시 UserNotFoundException이 발생한다")
        void getMyProfile_UserNotFound() {
            // given
            Long nonExistentUserId = 999L;
            given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userProfileService.getMyProfile(nonExistentUserId))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining("999");

            // 검증
            verify(userRepository).findById(nonExistentUserId);
        }

        @Test
        @DisplayName("성공: 관리자 권한 사용자가 자신의 프로필을 조회한다")
        void getMyProfile_AdminUser() {
            // given
            User adminUser = mock(User.class);
            when(adminUser.getUserId()).thenReturn(2L);
            when(adminUser.getEmail()).thenReturn("admin@example.com");
            when(adminUser.getUsername()).thenReturn("관리자");
            when(adminUser.getDepartment()).thenReturn("관리팀");
            when(adminUser.getRole()).thenReturn(UserRole.ADMIN);
            when(adminUser.getTokenUsage()).thenReturn(5000L);
            when(adminUser.getCreatedAt()).thenReturn(now.minusDays(60));
            when(adminUser.getUpdatedAt()).thenReturn(now);

            given(userRepository.findById(2L)).willReturn(Optional.of(adminUser));

            // when
            UserDetailResponse response = userProfileService.getMyProfile(2L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.userId()).isEqualTo(2L);
            assertThat(response.role()).isEqualTo(UserRole.ADMIN);
            assertThat(response.tokenUsage()).isEqualTo(5000L);

            // 검증
            verify(userRepository).findById(2L);
        }

        @Test
        @DisplayName("엣지케이스: null userId로 조회 시도시 UserException이 발생한다")
        void getMyProfile_NullUserId() {
            // when & then
            assertThatThrownBy(() -> userProfileService.getMyProfile(null))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining("userId cannot be null");

            // 검증: repository 호출이 되지 않아야 함
            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("엣지케이스: 토큰 사용량이 null인 사용자 프로필 조회")
        void getMyProfile_NullTokenUsage() {
            // given
            User userWithNullTokenUsage = mock(User.class);
            when(userWithNullTokenUsage.getUserId()).thenReturn(3L);
            when(userWithNullTokenUsage.getEmail()).thenReturn("newuser@example.com");
            when(userWithNullTokenUsage.getUsername()).thenReturn("신규유저");
            when(userWithNullTokenUsage.getDepartment()).thenReturn("신규팀");
            when(userWithNullTokenUsage.getRole()).thenReturn(UserRole.USER);
            when(userWithNullTokenUsage.getTokenUsage()).thenReturn(null);
            when(userWithNullTokenUsage.getCreatedAt()).thenReturn(now);
            when(userWithNullTokenUsage.getUpdatedAt()).thenReturn(now);

            given(userRepository.findById(3L)).willReturn(Optional.of(userWithNullTokenUsage));

            // when
            UserDetailResponse response = userProfileService.getMyProfile(3L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.tokenUsage()).isNull();

            // 검증
            verify(userRepository).findById(3L);
        }
    }

    @Nested
    @DisplayName("다른 사용자 프로필 조회")
    class GetUserProfile {

        @Test
        @DisplayName("성공: 관리자가 다른 사용자의 프로필을 조회한다")
        void getUserProfile_AdminCanViewOthers() {
            // given
            Long targetUserId = 5L;
            Long adminUserId = 1L;

            User targetUser = mock(User.class);
            when(targetUser.getUserId()).thenReturn(targetUserId);
            when(targetUser.getEmail()).thenReturn("target@example.com");
            when(targetUser.getUsername()).thenReturn("대상유저");
            when(targetUser.getDepartment()).thenReturn("마케팅팀");
            when(targetUser.getRole()).thenReturn(UserRole.USER);
            when(targetUser.getTokenUsage()).thenReturn(500L);
            when(targetUser.getCreatedAt()).thenReturn(now.minusDays(10));
            when(targetUser.getUpdatedAt()).thenReturn(now.minusHours(2));

            User adminUser = mock(User.class);
            when(adminUser.getRole()).thenReturn(UserRole.ADMIN);

            given(userRepository.findById(adminUserId)).willReturn(Optional.of(adminUser));
            given(userRepository.findById(targetUserId)).willReturn(Optional.of(targetUser));

            // when
            UserDetailResponse response = userProfileService.getUserProfile(adminUserId, targetUserId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.userId()).isEqualTo(targetUserId);
            assertThat(response.email()).isEqualTo("target@example.com");
            assertThat(response.department()).isEqualTo("마케팅팀");

            // 검증
            verify(userRepository).findById(adminUserId);
            verify(userRepository).findById(targetUserId);
        }

        @Test
        @DisplayName("실패: 일반 사용자가 다른 사용자의 프로필을 조회하려고 시도한다")
        void getUserProfile_RegularUserCannotViewOthers() {
            // given
            Long targetUserId = 5L;
            Long regularUserId = 1L;

            User regularUser = mock(User.class);
            when(regularUser.getUserId()).thenReturn(regularUserId);
            when(regularUser.getRole()).thenReturn(UserRole.USER);

            given(userRepository.findById(regularUserId)).willReturn(Optional.of(regularUser));

            // when & then
            assertThatThrownBy(() -> userProfileService.getUserProfile(regularUserId, targetUserId))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining("권한이 없습니다");

            // 검증: 권한 확인 후 target user 조회는 하지 않아야 함
            verify(userRepository).findById(regularUserId);
            verify(userRepository, never()).findById(targetUserId);
        }

    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("시나리오: 로그인 후 프로필 조회 플로우")
        void loginAndGetProfile_Scenario() {
            // given - 로그인된 사용자 시뮬레이션
            Long loggedInUserId = 1L;
            given(userRepository.findById(loggedInUserId)).willReturn(Optional.of(mockUser));

            // when - 프로필 조회
            UserDetailResponse profile = userProfileService.getMyProfile(loggedInUserId);

            // then
            assertThat(profile).isNotNull();
            assertThat(profile.userId()).isEqualTo(loggedInUserId);
            assertThat(profile.email()).isEqualTo("test@example.com");
            assertThat(profile.username()).isEqualTo("테스트유저");

            // 검증
            verify(userRepository).findById(loggedInUserId);
        }

        @Test
        @DisplayName("시나리오: 관리자가 여러 사용자의 프로필을 순차적으로 조회")
        void adminViewMultipleProfiles_Scenario() {
            // given
            Long adminId = 1L;
            User adminUser = mock(User.class);
            when(adminUser.getUserId()).thenReturn(adminId);
            when(adminUser.getRole()).thenReturn(UserRole.ADMIN);

            User user1 = mock(User.class);
            when(user1.getUserId()).thenReturn(2L);
            when(user1.getEmail()).thenReturn("user1@example.com");
            when(user1.getUsername()).thenReturn("유저1");
            when(user1.getDepartment()).thenReturn("개발팀");
            when(user1.getRole()).thenReturn(UserRole.USER);
            when(user1.getTokenUsage()).thenReturn(100L);
            when(user1.getCreatedAt()).thenReturn(now);
            when(user1.getUpdatedAt()).thenReturn(now);

            User user2 = mock(User.class);
            when(user2.getUserId()).thenReturn(3L);
            when(user2.getEmail()).thenReturn("user2@example.com");
            when(user2.getUsername()).thenReturn("유저2");
            when(user2.getDepartment()).thenReturn("디자인팀");
            when(user2.getRole()).thenReturn(UserRole.USER);
            when(user2.getTokenUsage()).thenReturn(200L);
            when(user2.getCreatedAt()).thenReturn(now);
            when(user2.getUpdatedAt()).thenReturn(now);

            given(userRepository.findById(adminId)).willReturn(Optional.of(adminUser));
            given(userRepository.findById(2L)).willReturn(Optional.of(user1));
            given(userRepository.findById(3L)).willReturn(Optional.of(user2));

            // when - 여러 프로필 조회
            UserDetailResponse profile1 = userProfileService.getUserProfile(adminId, 2L);
            UserDetailResponse profile2 = userProfileService.getUserProfile(adminId, 3L);

            // then
            assertThat(profile1.email()).isEqualTo("user1@example.com");
            assertThat(profile1.department()).isEqualTo("개발팀");
            assertThat(profile2.email()).isEqualTo("user2@example.com");
            assertThat(profile2.department()).isEqualTo("디자인팀");

            // 검증
            verify(userRepository, times(2)).findById(adminId);
            verify(userRepository).findById(2L);
            verify(userRepository).findById(3L);
        }
    }

    @Nested
    @DisplayName("마이페이지 업데이트")
    class UpdateMyProfile {

        @Test
        @DisplayName("성공: username과 department 모두 업데이트")
        void updateMyProfile_Success_BothFields() {
            // given
            String newUsername = "새로운이름";
            String newDepartment = "새로운부서";
            UpdateProfileRequest request = new UpdateProfileRequest(newUsername, newDepartment);

            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

            // when
            UserDetailResponse response = userProfileService.updateMyProfile(userId, request);

            // then
            verify(mockUser).updateProfile(newUsername, newDepartment);
            verify(userRepository).findById(userId);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("성공: username만 업데이트 (department null)")
        void updateMyProfile_Success_UsernameOnly() {
            // given
            String newUsername = "새로운이름";
            UpdateProfileRequest request = new UpdateProfileRequest(newUsername, null);

            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

            // when
            UserDetailResponse response = userProfileService.updateMyProfile(userId, request);

            // then
            verify(mockUser).updateProfile(newUsername, null);
            verify(userRepository).findById(userId);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("성공: department만 업데이트")
        void updateMyProfile_Success_DepartmentOnly() {
            // given
            String existingUsername = "기존이름";
            String newDepartment = "새로운부서";
            UpdateProfileRequest request = new UpdateProfileRequest(existingUsername, newDepartment);

            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

            // when
            UserDetailResponse response = userProfileService.updateMyProfile(userId, request);

            // then
            verify(mockUser).updateProfile(existingUsername, newDepartment);
            verify(userRepository).findById(userId);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("성공: 변경사항 없음 (동일한 값)")
        void updateMyProfile_Success_NoChanges() {
            // given
            String sameUsername = "테스트유저";
            String sameDepartment = "개발팀";
            UpdateProfileRequest request = new UpdateProfileRequest(sameUsername, sameDepartment);

            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

            // when
            UserDetailResponse response = userProfileService.updateMyProfile(userId, request);

            // then
            verify(mockUser).updateProfile(sameUsername, sameDepartment);
            verify(userRepository).findById(userId);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("실패: 존재하지 않는 userId")
        void updateMyProfile_UserNotFound() {
            // given
            Long nonExistentUserId = 999L;
            UpdateProfileRequest request = new UpdateProfileRequest("새이름", "새부서");

            given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userProfileService.updateMyProfile(nonExistentUserId, request))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining("999");

            verify(userRepository).findById(nonExistentUserId);
        }

        @Test
        @DisplayName("실패: null userId")
        void updateMyProfile_NullUserId() {
            // given
            UpdateProfileRequest request = new UpdateProfileRequest("새이름", "새부서");

            // when & then
            assertThatThrownBy(() -> userProfileService.updateMyProfile(null, request))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining("userId cannot be null");

            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("실패: username이 빈 문자열")
        void updateMyProfile_EmptyUsername() {
            // given
            User realUser = User.builder()
                    .email("test@example.com")
                    .username("기존이름")
                    .department("기존부서")
                    .password("password")
                    .role(UserRole.USER)
                    .build();

            // when & then
            assertThatThrownBy(() -> realUser.updateProfile("", "부서"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용자 이름은 필수입니다.");
        }

        @Test
        @DisplayName("실패: username이 null")
        void updateMyProfile_NullUsername() {
            // given
            User realUser = User.builder()
                    .email("test@example.com")
                    .username("기존이름")
                    .department("기존부서")
                    .password("password")
                    .role(UserRole.USER)
                    .build();

            // when & then
            assertThatThrownBy(() -> realUser.updateProfile(null, "부서"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용자 이름은 필수입니다.");
        }

        @Test
        @DisplayName("엣지케이스: department를 null로 변경 (기존값 제거)")
        void updateMyProfile_EdgeCase_RemoveDepartment() {
            // given
            String newUsername = "새이름";
            UpdateProfileRequest request = new UpdateProfileRequest(newUsername, null);

            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

            // when
            UserDetailResponse response = userProfileService.updateMyProfile(userId, request);

            // then
            verify(mockUser).updateProfile(newUsername, null);
            verify(userRepository).findById(userId);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("엣지케이스: username 공백만 입력")
        void updateMyProfile_EdgeCase_WhitespaceUsername() {
            // given
            User realUser = User.builder()
                    .email("test@example.com")
                    .username("기존이름")
                    .department("기존부서")
                    .password("password")
                    .role(UserRole.USER)
                    .build();

            // when & then
            assertThatThrownBy(() -> realUser.updateProfile("   ", "부서"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용자 이름은 필수입니다");
        }
    }
}