package io.github.cryschan.berepository.domain.blog.service;

import io.github.cryschan.berepository.domain.blog.dto.response.BlogPageResponse;
import io.github.cryschan.berepository.domain.blog.dto.response.BlogResponse;
import io.github.cryschan.berepository.domain.blog.entity.Blog;
import io.github.cryschan.berepository.domain.blog.exception.BlogException;
import io.github.cryschan.berepository.domain.blog.repository.BlogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("BlogService 테스트")
class BlogServiceTest {

    @Mock
    private BlogRepository blogRepository;

    @InjectMocks
    private BlogService blogService;

    private Blog blog1;
    private Blog blog2;
    private Blog blog3;

    @BeforeEach
    void setUp() {
        blog1 = Blog.builder()
                .blogTemplateId(1L)
                .title("여름 남성 반팔 티셔츠 추천")
                .content("여름철 필수 아이템...")
                .category("남성 의류")
                .userId(1L)
                .build();

        blog2 = Blog.builder()
                .blogTemplateId(1L)
                .title("메이크업 초보자 가이드")
                .content("초보자도 쉽게...")
                .category("메이크업 제품")
                .userId(1L)
                .build();

        blog3 = Blog.builder()
                .blogTemplateId(2L)
                .title("편안한 운동화 추천")
                .content("일상생활에서...")
                .category("신발")
                .userId(2L)
                .build();
    }

    @Nested
    @DisplayName("블로그 목록 조회 테스트")
    class GetMyBlogsTest {

        @Test
        @DisplayName("성공: 정상적인 페이지 번호로 블로그 목록을 조회한다")
        void getMyBlogs_Success() {
            // given
            Long userId = 1L;
            int page = 1;
            Pageable pageable = PageRequest.of(0, 4);
            Page<Blog> blogPage = new PageImpl<>(List.of(blog1, blog2), pageable, 2);

            given(blogRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable))
                    .willReturn(blogPage);

            // when
            BlogPageResponse response = blogService.getMyBlogs(userId, page);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getBlogs()).hasSize(2);
            assertThat(response.getCurrentPage()).isEqualTo(1);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(2);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();

            verify(blogRepository).findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        @Test
        @DisplayName("실패: 페이지 번호가 0인 경우 BlogException이 발생한다")
        void getMyBlogs_Fail_PageNumberZero() {
            // given
            Long userId = 1L;
            int page = 0;

            // when & then
            assertThatThrownBy(() -> blogService.getMyBlogs(userId, page))
                    .isInstanceOf(BlogException.class)
                    .hasMessageContaining("페이지 번호는 1 이상이어야 합니다");

            verify(blogRepository, never()).findAllByUserIdOrderByCreatedAtDesc(any(), any());
        }

        @Test
        @DisplayName("실패: 페이지 번호가 음수인 경우 BlogException이 발생한다")
        void getMyBlogs_Fail_PageNumberNegative() {
            // given
            Long userId = 1L;
            int page = -1;

            // when & then
            assertThatThrownBy(() -> blogService.getMyBlogs(userId, page))
                    .isInstanceOf(BlogException.class)
                    .hasMessageContaining("페이지 번호는 1 이상이어야 합니다");

            verify(blogRepository, never()).findAllByUserIdOrderByCreatedAtDesc(any(), any());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 페이지 번호로 조회시 BlogException이 발생한다")
        void getMyBlogs_Fail_PageNotExists() {
            // given
            Long userId = 1L;
            int page = 10;
            Pageable pageable = PageRequest.of(9, 4);
            Page<Blog> emptyPage = new PageImpl<>(List.of(), pageable, 5);

            given(blogRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable))
                    .willReturn(emptyPage);

            // when & then
            assertThatThrownBy(() -> blogService.getMyBlogs(userId, page))
                    .isInstanceOf(BlogException.class)
                    .hasMessageContaining("페이지")
                    .hasMessageContaining("존재하지 않습니다");

            verify(blogRepository).findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        @Test
        @DisplayName("성공: 블로그가 없는 사용자도 빈 목록을 정상적으로 조회한다")
        void getMyBlogs_Success_EmptyBlogs() {
            // given
            Long userId = 999L;
            int page = 1;
            Pageable pageable = PageRequest.of(0, 4);
            Page<Blog> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(blogRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable))
                    .willReturn(emptyPage);

            // when
            BlogPageResponse response = blogService.getMyBlogs(userId, page);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getBlogs()).isEmpty();
            assertThat(response.getCurrentPage()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(0);

            verify(blogRepository).findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
    }

    @Nested
    @DisplayName("블로그 상세 조회 테스트")
    class GetBlogTest {

        @Test
        @DisplayName("성공: 유효한 블로그 ID로 블로그를 조회한다")
        void getBlog_Success() {
            // given
            Long blogId = 1L;
            given(blogRepository.findById(blogId)).willReturn(Optional.of(blog1));

            // when
            BlogResponse response = blogService.getBlog(blogId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("여름 남성 반팔 티셔츠 추천");
            assertThat(response.getCategory()).isEqualTo("남성 의류");

            verify(blogRepository).findById(blogId);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 블로그 ID로 조회시 BlogException이 발생한다")
        void getBlog_Fail_BlogNotFound() {
            // given
            Long blogId = 999L;
            given(blogRepository.findById(blogId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> blogService.getBlog(blogId))
                    .isInstanceOf(BlogException.class)
                    .hasMessageContaining("블로그를 찾을 수 없습니다")
                    .hasMessageContaining("999");

            verify(blogRepository).findById(blogId);
        }

        @Test
        @DisplayName("실패: null 블로그 ID로 조회시 BlogException이 발생한다")
        void getBlog_Fail_NullId() {
            // given
            Long blogId = null;

            // when & then
            assertThatThrownBy(() -> blogService.getBlog(blogId))
                    .isInstanceOf(BlogException.class)
                    .hasMessageContaining("유효하지 않은 블로그 ID입니다");

            verify(blogRepository, never()).findById(any());
        }

        @Test
        @DisplayName("실패: 0 이하의 블로그 ID로 조회시 BlogException이 발생한다")
        void getBlog_Fail_InvalidId() {
            // given
            Long blogId = 0L;

            // when & then
            assertThatThrownBy(() -> blogService.getBlog(blogId))
                    .isInstanceOf(BlogException.class)
                    .hasMessageContaining("유효하지 않은 블로그 ID입니다");

            verify(blogRepository, never()).findById(any());
        }

        @Test
        @DisplayName("실패: 음수 블로그 ID로 조회시 BlogException이 발생한다")
        void getBlog_Fail_NegativeId() {
            // given
            Long blogId = -1L;

            // when & then
            assertThatThrownBy(() -> blogService.getBlog(blogId))
                    .isInstanceOf(BlogException.class)
                    .hasMessageContaining("유효하지 않은 블로그 ID입니다");

            verify(blogRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("권한 검증 포함 블로그 조회 테스트")
    class GetBlogWithAuthTest {

        @Test
        @DisplayName("성공: 작성자가 자신의 블로그를 조회한다")
        void getBlogWithAuth_Success() {
            // given
            Long blogId = 1L;
            Long userId = 1L;
            given(blogRepository.findById(blogId)).willReturn(Optional.of(blog1));

            // when
            BlogResponse response = blogService.getBlogWithAuth(blogId, userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);

            verify(blogRepository).findById(blogId);
        }

        @Test
        @DisplayName("실패: 다른 사용자의 블로그 조회시 BlogException이 발생한다")
        void getBlogWithAuth_Fail_AccessDenied() {
            // given
            Long blogId = 1L;
            Long userId = 2L;  // 다른 사용자
            given(blogRepository.findById(blogId)).willReturn(Optional.of(blog1));

            // when & then
            assertThatThrownBy(() -> blogService.getBlogWithAuth(blogId, userId))
                    .isInstanceOf(BlogException.class)
                    .hasMessageContaining("이 블로그에 접근할 권한이 없습니다");

            verify(blogRepository).findById(blogId);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 블로그에 대한 권한 검증시 BlogException이 발생한다")
        void getBlogWithAuth_Fail_BlogNotFound() {
            // given
            Long blogId = 999L;
            Long userId = 1L;
            given(blogRepository.findById(blogId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> blogService.getBlogWithAuth(blogId, userId))
                    .isInstanceOf(BlogException.class)
                    .hasMessageContaining("블로그를 찾을 수 없습니다");

            verify(blogRepository).findById(blogId);
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("시나리오: 사용자가 자신의 블로그 목록을 조회하고 상세 조회한다")
        void viewMyBlogsAndDetail_Scenario() {
            // given
            Long userId = 1L;
            int page = 1;
            Pageable pageable = PageRequest.of(0, 4);
            Page<Blog> blogPage = new PageImpl<>(List.of(blog1, blog2), pageable, 2);

            given(blogRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable))
                    .willReturn(blogPage);
            given(blogRepository.findById(1L)).willReturn(Optional.of(blog1));

            // 1. 블로그 목록 조회
            BlogPageResponse listResponse = blogService.getMyBlogs(userId, page);
            assertThat(listResponse.getBlogs()).hasSize(2);
            assertThat(listResponse.getBlogs().get(0).getId()).isEqualTo(1L);

            // 2. 첫 번째 블로그 상세 조회
            BlogResponse detailResponse = blogService.getBlog(1L);
            assertThat(detailResponse.getId()).isEqualTo(1L);
            assertThat(detailResponse.getTitle()).isEqualTo("여름 남성 반팔 티셔츠 추천");

            // 검증
            verify(blogRepository).findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
            verify(blogRepository).findById(1L);
        }

        @Test
        @DisplayName("시나리오: 잘못된 페이지 번호로 조회 후 올바른 페이지로 재시도")
        void retryWithValidPage_Scenario() {
            // given
            Long userId = 1L;
            int invalidPage = 0;
            int validPage = 1;
            Pageable pageable = PageRequest.of(0, 4);
            Page<Blog> blogPage = new PageImpl<>(List.of(blog1, blog2), pageable, 2);

            given(blogRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable))
                    .willReturn(blogPage);

            // 1. 잘못된 페이지 번호로 조회 시도
            assertThatThrownBy(() -> blogService.getMyBlogs(userId, invalidPage))
                    .isInstanceOf(BlogException.class);

            // 2. 올바른 페이지 번호로 재시도
            BlogPageResponse response = blogService.getMyBlogs(userId, validPage);
            assertThat(response.getBlogs()).hasSize(2);

            // 검증
            verify(blogRepository).findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        @Test
        @DisplayName("시나리오: 여러 페이지 순차 조회")
        void navigateMultiplePages_Scenario() {
            // given
            Long userId = 1L;
            Pageable page1 = PageRequest.of(0, 4);
            Pageable page2 = PageRequest.of(1, 4);

            Page<Blog> firstPage = new PageImpl<>(List.of(blog1, blog2), page1, 6);
            Page<Blog> secondPage = new PageImpl<>(List.of(blog3), page2, 6);

            given(blogRepository.findAllByUserIdOrderByCreatedAtDesc(userId, page1))
                    .willReturn(firstPage);
            given(blogRepository.findAllByUserIdOrderByCreatedAtDesc(userId, page2))
                    .willReturn(secondPage);

            // 1. 첫 번째 페이지 조회
            BlogPageResponse response1 = blogService.getMyBlogs(userId, 1);
            assertThat(response1.getCurrentPage()).isEqualTo(1);
            assertThat(response1.getBlogs()).hasSize(2);
            assertThat(response1.isFirst()).isTrue();
            assertThat(response1.isLast()).isFalse();

            // 2. 두 번째 페이지 조회
            BlogPageResponse response2 = blogService.getMyBlogs(userId, 2);
            assertThat(response2.getCurrentPage()).isEqualTo(2);
            assertThat(response2.getBlogs()).hasSize(1);
            assertThat(response2.isFirst()).isFalse();

            // 검증
            verify(blogRepository).findAllByUserIdOrderByCreatedAtDesc(userId, page1);
            verify(blogRepository).findAllByUserIdOrderByCreatedAtDesc(userId, page2);
        }
    }
}
    @Nested
    @DisplayName("블로그 수정 테스트")
    class UpdateBlogTest {

        private BlogUpdateRequest validUpdateRequest;

        @BeforeEach
        void setUpUpdateRequest() {
            validUpdateRequest = createBlogUpdateRequest(
                    "수정된 제목",
                    "수정된 내용입니다.",
                    "하의",
                    2L
            );
        }

        @Test
        @DisplayName("성공: 블로그를 정상적으로 수정한다")
        void updateBlog_Success() {
            Long blogId = 1L;
            Long userId = 1L;
            given(blogRepository.findById(blogId)).willReturn(Optional.of(blog1));

            BlogResponse response = blogService.updateBlog(blogId, userId, validUpdateRequest);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("수정된 제목");
            assertThat(response.getContent()).isEqualTo("수정된 내용입니다.");
            assertThat(response.getCategory()).isEqualTo("하의");
            assertThat(response.getBlogTemplateId()).isEqualTo(2L);
            verify(blogRepository).findById(blogId);
        }

        @Test
        @DisplayName("성공: 제목만 수정한다")
        void updateBlog_Title_Only() {
            Long blogId = 1L;
            Long userId = 1L;
            BlogUpdateRequest request = createBlogUpdateRequest(
                    "새로운 제목",
                    blog1.getContent(),
                    blog1.getCategory(),
                    blog1.getBlogTemplateId()
            );
            given(blogRepository.findById(blogId)).willReturn(Optional.of(blog1));

            BlogResponse response = blogService.updateBlog(blogId, userId, request);

            assertThat(response.getTitle()).isEqualTo("새로운 제목");
            assertThat(response.getContent()).isEqualTo(blog1.getContent());
            verify(blogRepository).findById(blogId);
        }

        @Test
        @DisplayName("성공: 마크다운 형식의 이미지가 포함된 내용을 수정한다")
        void updateBlog_With_Markdown_Images() {
            Long blogId = 1L;
            Long userId = 1L;
            String contentWithImages = """
                    # 여름 반팔 티셔츠 추천
                    
                    ![티셔츠1](https://bucket.s3.ap-northeast-2.amazonaws.com/uploads/550e8400-e29b-41d4-a716-446655440000_tshirt1.jpg)
                    
                    시원한 소재로 만들어진 티셔츠입니다.
                    """;
            BlogUpdateRequest request = createBlogUpdateRequest("이미지가 포함된 블로그", contentWithImages, "상의", 1L);
            given(blogRepository.findById(blogId)).willReturn(Optional.of(blog1));

            BlogResponse response = blogService.updateBlog(blogId, userId, request);

            assertThat(response.getContent()).contains("![티셔츠1]");
            assertThat(response.getContent()).contains("s3.ap-northeast-2.amazonaws.com");
            verify(blogRepository).findById(blogId);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 블로그를 수정하려고 하면 BlogException이 발생한다")
        void updateBlog_Fail_BlogNotFound() {
            Long blogId = 999L;
            Long userId = 1L;
            given(blogRepository.findById(blogId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> blogService.updateBlog(blogId, userId, validUpdateRequest))
                    .isInstanceOf(BlogException.class)
                    .hasMessageContaining("블로그를 찾을 수 없습니다");

            verify(blogRepository).findById(blogId);
        }

        @Test
        @DisplayName("실패: 다른 사용자의 블로그를 수정하려고 하면 BlogException이 발생한다")
        void updateBlog_Fail_Forbidden() {
            Long blogId = 1L;
            Long userId = 2L;
            given(blogRepository.findById(blogId)).willReturn(Optional.of(blog1));

            assertThatThrownBy(() -> blogService.updateBlog(blogId, userId, validUpdateRequest))
                    .isInstanceOf(BlogException.class)
                    .hasMessageContaining("권한이 없습니다");

            verify(blogRepository).findById(blogId);
        }

        @Test
        @DisplayName("성공: 모든 필드를 동시에 수정한다")
        void updateBlog_All_Fields() {
            Long blogId = 1L;
            Long userId = 1L;
            BlogUpdateRequest request = createBlogUpdateRequest(
                    "완전히 새로운 제목",
                    "완전히 새로운 내용입니다.",
                    "신발",
                    3L
            );
            given(blogRepository.findById(blogId)).willReturn(Optional.of(blog1));

            BlogResponse response = blogService.updateBlog(blogId, userId, request);

            assertThat(response.getTitle()).isEqualTo("완전히 새로운 제목");
            assertThat(response.getContent()).isEqualTo("완전히 새로운 내용입니다.");
            assertThat(response.getCategory()).isEqualTo("신발");
            assertThat(response.getBlogTemplateId()).isEqualTo(3L);
            verify(blogRepository).findById(blogId);
        }
    }

    private BlogUpdateRequest createBlogUpdateRequest(String title, String content, String category, Long blogTemplateId) {
        try {
            BlogUpdateRequest request = new BlogUpdateRequest();
            
            java.lang.reflect.Field titleField = BlogUpdateRequest.class.getDeclaredField("title");
            titleField.setAccessible(true);
            titleField.set(request, title);

            java.lang.reflect.Field contentField = BlogUpdateRequest.class.getDeclaredField("content");
            contentField.setAccessible(true);
            contentField.set(request, content);

            java.lang.reflect.Field categoryField = BlogUpdateRequest.class.getDeclaredField("category");
            categoryField.setAccessible(true);
            categoryField.set(request, category);

            java.lang.reflect.Field blogTemplateIdField = BlogUpdateRequest.class.getDeclaredField("blogTemplateId");
            blogTemplateIdField.setAccessible(true);
            blogTemplateIdField.set(request, blogTemplateId);

            return request;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create BlogUpdateRequest", e);
        }
    }
}