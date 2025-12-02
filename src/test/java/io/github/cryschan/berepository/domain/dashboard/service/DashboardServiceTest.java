package io.github.cryschan.berepository.domain.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cryschan.berepository.domain.blog.entity.Blog;
import io.github.cryschan.berepository.domain.blog.entity.BlogPublishStatus;
import io.github.cryschan.berepository.domain.blog.repository.BlogRepository;
import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.repository.BlogTemplateRepository;
import io.github.cryschan.berepository.domain.dashboard.dto.DashboardResponse;
import io.github.cryschan.berepository.domain.dashboard.repository.DashboardRepository;
import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.cryschan.berepository.domain.user.entity.role.UserRole.ADMIN;
import static io.github.cryschan.berepository.domain.user.entity.role.UserRole.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService 테스트")
class DashboardServiceTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private BlogTemplateRepository blogTemplateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DashboardRepository dashboardRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DashboardService dashboardService;

    private BlogTemplate template1;
    private BlogTemplate template2;
    private Blog blog1;
    private Blog blog2;
    private Blog blog3;
    private Blog todayBlog;
    private User adminUser;
    private Long adminUserId = 1L;

    @BeforeEach
    void setUp() {
        // Admin 사용자 데이터 준비
        adminUser = User.builder()
                .email("admin@test.com")
                .password("password")
                .username("admin")
                .role(ADMIN)
                .department("관리부")
                .build();

        // 템플릿 데이터 준비
        template1 = BlogTemplate.builder()
                .id(1L)
                .title("패션 템플릿")
                .categories(List.of("패션", "의류"))
                .platforms(List.of("네이버", "카카오"))
                .shopUrl("https://shop1.com")
                .includeImages(true)
                .imageCount(3)
                .charLimit(1000)
                .dailyPostTime(LocalTime.of(10, 0))
                .build();

        template2 = BlogTemplate.builder()
                .id(2L)
                .title("뷰티 템플릿")
                .categories(List.of("뷰티", "화장품"))
                .platforms(List.of("네이버", "인스타그램"))
                .shopUrl("https://shop2.com")
                .includeImages(true)
                .imageCount(5)
                .charLimit(2000)
                .dailyPostTime(LocalTime.of(14, 0))
                .build();

        // 블로그 데이터 준비
        blog1 = Blog.builder()
                .blogTemplateId(1L)
                .title("패션 블로그 1")
                .content("내용 1")
                .category("패션")
                .userId(1L)
                .publishStatus(BlogPublishStatus.PUBLISHED)
                .build();
        ReflectionTestUtils.setField(blog1, "id", 1L);

        blog2 = Blog.builder()
                .blogTemplateId(1L)
                .title("패션 블로그 2")
                .content("내용 2")
                .category("패션")
                .userId(2L)
                .publishStatus(BlogPublishStatus.PUBLISHED)
                .build();
        ReflectionTestUtils.setField(blog2, "id", 2L);

        blog3 = Blog.builder()
                .blogTemplateId(2L)
                .title("뷰티 블로그 1")
                .content("내용 3")
                .category("뷰티")
                .userId(3L)
                .publishStatus(BlogPublishStatus.PUBLISHED)
                .build();
        ReflectionTestUtils.setField(blog3, "id", 3L);

        // 오늘 작성된 블로그
        todayBlog = Blog.builder()
                .blogTemplateId(1L)
                .title("오늘의 블로그")
                .content("오늘 작성된 내용")
                .category("패션")
                .userId(1L)
                .publishStatus(BlogPublishStatus.PUBLISHED)
                .build();
        ReflectionTestUtils.setField(todayBlog, "id", 4L);
    }

    @Nested
    @DisplayName("대시보드 데이터 조회 테스트")
    class GetDashboardDataTest {

        @Test
        @DisplayName("성공: 모든 대시보드 데이터가 정상적으로 조회된다")
        void getDashboardData_Success() {
            // given
            List<Blog> allBlogs = List.of(blog1, blog2, blog3);
            List<BlogTemplate> allTemplates = List.of(template1, template2);
            List<Blog> todayBlogs = List.of(todayBlog);

            // Admin 권한 체크를 위한 모킹
            given(userRepository.findById(adminUserId)).willReturn(Optional.of(adminUser));
            given(blogRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(1);
            given(blogRepository.count()).willReturn(3L);
            given(userRepository.countByRole(USER)).willReturn(10);
            given(blogRepository.findAll()).willReturn(allBlogs);
            given(blogTemplateRepository.findAll()).willReturn(allTemplates);
            given(blogRepository.findAllByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(todayBlogs);
            given(userRepository.findAll()).willReturn(new ArrayList<>());

            // when
            DashboardResponse response = dashboardService.getDashboardData(adminUserId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getActiveUserCount()).isEqualTo(10);
            assertThat(response.getTodayBlogCount()).isEqualTo(1);
            assertThat(response.getTotalBlogCount()).isEqualTo(3);

            // 카테고리 분포 검증 (Blog의 category 기준)
            assertThat(response.getCategoryDistribution()).isNotNull();
            assertThat(response.getCategoryDistribution().get("패션")).isEqualTo(2L); // blog1, blog2
            assertThat(response.getCategoryDistribution().get("뷰티")).isEqualTo(1L); // blog3

            // 플랫폼 분포 검증
            assertThat(response.getPlatformUsage()).isNotNull();
            assertThat(response.getPlatformUsage().get("네이버")).isEqualTo(3L); // blog1, blog2, blog3
            assertThat(response.getPlatformUsage().get("카카오")).isEqualTo(2L); // blog1, blog2
            assertThat(response.getPlatformUsage().get("인스타그램")).isEqualTo(1L); // blog3

            // 오늘 작성된 블로그 리스트 검증
            assertThat(response.getTodayBlogItemList()).isNotNull();
            assertThat(response.getTodayBlogItemList()).hasSize(1);
            assertThat(response.getTodayBlogItemList().get(0).getTitle()).isEqualTo("오늘의 블로그");

            // 검증: 메서드 호출 확인
            verify(userRepository).findById(adminUserId);
            verify(blogRepository).countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
            verify(blogRepository).count();
            verify(userRepository).countByRole(USER);
            // findAll은 calculateDistributionAndUsage에서 한 번만 호출됨
            verify(blogRepository).findAll();
            // getTemplateMap()이 한 번만 호출되므로 findAll()도 1번만 호출됨
            verify(blogTemplateRepository).findAll();
            verify(blogRepository).findAllByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
            // getUserMap()과 getTotalTokenUsage()에서 각각 한 번씩 호출되므로 총 2번
            verify(userRepository, times(2)).findAll();
        }

        @Test
        @DisplayName("성공: 데이터가 없을 때 빈 결과를 반환한다")
        void getDashboardData_Success_EmptyData() {
            // given
            given(userRepository.findById(adminUserId)).willReturn(Optional.of(adminUser));
            given(blogRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(0);
            given(blogRepository.count()).willReturn(0L);
            given(userRepository.countByRole(USER)).willReturn(0);
            given(blogRepository.findAll()).willReturn(new ArrayList<>());
            given(blogTemplateRepository.findAll()).willReturn(new ArrayList<>());
            given(blogRepository.findAllByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(new ArrayList<>());
            given(userRepository.findAll()).willReturn(new ArrayList<>());

            // when
            DashboardResponse response = dashboardService.getDashboardData(adminUserId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getActiveUserCount()).isEqualTo(0);
            assertThat(response.getTodayBlogCount()).isEqualTo(0);
            assertThat(response.getTotalBlogCount()).isEqualTo(0);
            assertThat(response.getCategoryDistribution()).isEmpty();
            assertThat(response.getPlatformUsage()).isEmpty();
            assertThat(response.getTodayBlogItemList()).isEmpty();
        }

        @Test
        @DisplayName("실패: 존재하지 않는 템플릿 ID를 가진 블로그는 예외가 발생한다")
        void getDashboardData_Failure_InvalidTemplateId() {
            // given
            // 존재하지 않는 templateId를 가진 블로그
            Blog invalidBlog = Blog.builder()
                    .blogTemplateId(999L)  // 존재하지 않는 템플릿 ID
                    .title("유효하지 않은 템플릿 블로그")
                    .content("내용")
                    .category("기타")
                    .userId(1L)
                    .publishStatus(BlogPublishStatus.PUBLISHED)
                    .build();
            ReflectionTestUtils.setField(invalidBlog, "id", 999L);

            List<Blog> allBlogs = List.of(blog1, invalidBlog);
            List<BlogTemplate> allTemplates = List.of(template1);

            given(userRepository.findById(adminUserId)).willReturn(Optional.of(adminUser));
            given(blogRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(0);
            given(blogRepository.count()).willReturn(2L);
            given(userRepository.countByRole(USER)).willReturn(5);
            given(blogRepository.findAll()).willReturn(allBlogs);
            given(blogTemplateRepository.findAll()).willReturn(allTemplates);
            // 예외가 발생하면 이후 코드는 실행되지 않으므로 필요한 stubbing만 설정
            given(userRepository.findAll()).willReturn(new ArrayList<>());

            // when & then
            // calculateDistributionAndUsage에서 예외 발생
            assertThatThrownBy(() -> dashboardService.getDashboardData(adminUserId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("블로그 템플릿을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("성공: 플랫폼이 없는 템플릿의 경우 Unknown으로 표시된다")
        void getDashboardData_Success_NoPlatform() {
            // given
            // 플랫폼이 없는 템플릿
            BlogTemplate noPlatformTemplate = BlogTemplate.builder()
                    .id(3L)
                    .title("플랫폼 없음 템플릿")
                    .categories(List.of("기타"))
                    .platforms(new ArrayList<>()) // 빈 플랫폼 리스트
                    .shopUrl("https://shop3.com")
                    .includeImages(false)
                    .imageCount(0)
                    .charLimit(500)
                    .dailyPostTime(LocalTime.of(12, 0))
                    .build();

            Blog blogWithNoPlatform = Blog.builder()
                    .blogTemplateId(3L)
                    .title("플랫폼 없는 블로그")
                    .content("내용")
                    .category("기타")
                    .userId(1L)
                    .publishStatus(BlogPublishStatus.PUBLISHED)
                    .build();
            ReflectionTestUtils.setField(blogWithNoPlatform, "id", 5L);

            List<Blog> todayBlogs = List.of(blogWithNoPlatform);
            List<BlogTemplate> allTemplates = List.of(noPlatformTemplate);

            given(userRepository.findById(adminUserId)).willReturn(Optional.of(adminUser));
            given(blogRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(1);
            given(blogRepository.count()).willReturn(1L);
            given(userRepository.countByRole(USER)).willReturn(1);
            given(blogRepository.findAll()).willReturn(new ArrayList<>());
            given(blogTemplateRepository.findAll()).willReturn(allTemplates);
            given(blogRepository.findAllByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(todayBlogs);
            given(userRepository.findAll()).willReturn(new ArrayList<>());

            // when
            DashboardResponse response = dashboardService.getDashboardData(adminUserId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getTodayBlogItemList()).hasSize(1);
            assertThat(response.getTodayBlogItemList().get(0).getPlatform()).isEqualTo("Unknown");
        }
    }
}

