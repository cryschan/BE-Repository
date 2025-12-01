package io.github.cryschan.berepository.domain.blog.service;

import io.github.cryschan.berepository.domain.ai.dto.response.SsadaguSummaryResponse;
import io.github.cryschan.berepository.domain.blog.dto.response.BlogPageResponse;
import io.github.cryschan.berepository.domain.blog.dto.response.BlogResponse;
import io.github.cryschan.berepository.domain.blog.dto.response.BlogSaveResult;
import io.github.cryschan.berepository.domain.blog.entity.Blog;
import io.github.cryschan.berepository.domain.blog.exception.BlogException;
import io.github.cryschan.berepository.domain.blog.repository.BlogRepository;
import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
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
        ReflectionTestUtils.setField(blog1, "id", 1L);

        blog2 = Blog.builder()
                .blogTemplateId(1L)
                .title("메이크업 초보자 가이드")
                .content("초보자도 쉽게...")
                .category("메이크업 제품")
                .userId(1L)
                .build();
        ReflectionTestUtils.setField(blog2, "id", 2L);

        blog3 = Blog.builder()
                .blogTemplateId(2L)
                .title("편안한 운동화 추천")
                .content("일상생활에서...")
                .category("신발")
                .userId(2L)
                .build();
        ReflectionTestUtils.setField(blog3, "id", 3L);
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

    @Nested
    @DisplayName("블로그 일괄 생성 테스트")
    class CreateBlogsFromSummariesTest {

        @Test
        @DisplayName("성공: 모든 요약이 정상적으로 블로그로 저장된다")
        void createBlogsFromSummaries_Success() {
            // given
            Long templateId = 1L;
            String templateTitle = "패션 추천";
            Long userId = 10L;

            SsadaguSummaryResponse response1 = createMockSummaryResponse("패딩");
            SsadaguSummaryResponse response2 = createMockSummaryResponse("운동화");
            List<SsadaguSummaryResponse> summaries = List.of(response1, response2);

            given(blogRepository.save(any(Blog.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            BlogSaveResult result = blogService.createBlogsFromSummaries(templateId, templateTitle, userId, summaries);

            // then
            assertThat(result.successCount()).isEqualTo(2);
            assertThat(result.failCount()).isEqualTo(0);
            assertThat(result.isAllSuccess()).isTrue();

            verify(blogRepository, times(2)).save(any(Blog.class));
        }

        @Test
        @DisplayName("부분 성공: null summary는 건너뛰고 나머지는 저장된다")
        void createBlogsFromSummaries_SkipNullSummary() {
            // given
            Long templateId = 1L;
            String templateTitle = "패션 추천";
            Long userId = 10L;

            SsadaguSummaryResponse validResponse = createMockSummaryResponse("패딩");
            List<SsadaguSummaryResponse> summaries = new ArrayList<>();
            summaries.add(null);
            summaries.add(validResponse);

            given(blogRepository.save(any(Blog.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            BlogSaveResult result = blogService.createBlogsFromSummaries(templateId, templateTitle, userId, summaries);

            // then
            assertThat(result.successCount()).isEqualTo(1);
            assertThat(result.failCount()).isEqualTo(1);
            assertThat(result.isAllSuccess()).isFalse();

            verify(blogRepository, times(1)).save(any(Blog.class));
        }

        @Test
        @DisplayName("부분 성공: null product는 건너뛰고 나머지는 저장된다")
        void createBlogsFromSummaries_SkipNullProduct() {
            // given
            Long templateId = 1L;
            String templateTitle = "패션 추천";
            Long userId = 10L;

            SsadaguSummaryResponse nullProductResponse = SsadaguSummaryResponse.from(null, "요약 내용");
            SsadaguSummaryResponse validResponse = createMockSummaryResponse("패딩");
            List<SsadaguSummaryResponse> summaries = List.of(nullProductResponse, validResponse);

            given(blogRepository.save(any(Blog.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            BlogSaveResult result = blogService.createBlogsFromSummaries(templateId, templateTitle, userId, summaries);

            // then
            assertThat(result.successCount()).isEqualTo(1);
            assertThat(result.failCount()).isEqualTo(1);

            verify(blogRepository, times(1)).save(any(Blog.class));
        }

        @Test
        @DisplayName("부분 성공: null category는 건너뛰고 나머지는 저장된다")
        void createBlogsFromSummaries_SkipNullCategory() {
            // given
            Long templateId = 1L;
            String templateTitle = "패션 추천";
            Long userId = 10L;

            SsadaguProductDto nullCategoryProduct = SsadaguProductDto.builder()
                    .productName("테스트 상품")
                    .productUrl("https://test.com")
                    .price(10000)
                    .category(null)  // null category
                    .build();
            SsadaguSummaryResponse nullCategoryResponse = SsadaguSummaryResponse.from(nullCategoryProduct, "요약");
            SsadaguSummaryResponse validResponse = createMockSummaryResponse("패딩");
            List<SsadaguSummaryResponse> summaries = List.of(nullCategoryResponse, validResponse);

            given(blogRepository.save(any(Blog.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            BlogSaveResult result = blogService.createBlogsFromSummaries(templateId, templateTitle, userId, summaries);

            // then
            assertThat(result.successCount()).isEqualTo(1);
            assertThat(result.failCount()).isEqualTo(1);

            verify(blogRepository, times(1)).save(any(Blog.class));
        }

        @Test
        @DisplayName("부분 성공: 저장 중 예외 발생 시 다른 항목은 계속 저장된다")
        void createBlogsFromSummaries_ContinueOnException() {
            // given
            Long templateId = 1L;
            String templateTitle = "패션 추천";
            Long userId = 10L;

            SsadaguSummaryResponse response1 = createMockSummaryResponse("패딩");
            SsadaguSummaryResponse response2 = createMockSummaryResponse("운동화");
            List<SsadaguSummaryResponse> summaries = List.of(response1, response2);

            // 첫 번째 저장은 실패, 두 번째는 성공
            given(blogRepository.save(any(Blog.class)))
                    .willThrow(new RuntimeException("DB 오류"))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            BlogSaveResult result = blogService.createBlogsFromSummaries(templateId, templateTitle, userId, summaries);

            // then
            assertThat(result.successCount()).isEqualTo(1);
            assertThat(result.failCount()).isEqualTo(1);

            verify(blogRepository, times(2)).save(any(Blog.class));
        }

        @Test
        @DisplayName("성공: 빈 목록이 전달되면 아무것도 저장하지 않는다")
        void createBlogsFromSummaries_EmptyList() {
            // given
            Long templateId = 1L;
            String templateTitle = "패션 추천";
            Long userId = 10L;
            List<SsadaguSummaryResponse> summaries = List.of();

            // when
            BlogSaveResult result = blogService.createBlogsFromSummaries(templateId, templateTitle, userId, summaries);

            // then
            assertThat(result.successCount()).isEqualTo(0);
            assertThat(result.failCount()).isEqualTo(0);
            assertThat(result.totalCount()).isEqualTo(0);

            verify(blogRepository, never()).save(any(Blog.class));
        }

        @Test
        @DisplayName("성공: 긴 상품명은 50자로 잘린다")
        void createBlogsFromSummaries_TruncateLongProductName() {
            // given
            Long templateId = 1L;
            String templateTitle = "패션 추천";
            Long userId = 10L;

            String longProductName = "A".repeat(100);  // 100자 상품명
            SsadaguProductDto product = SsadaguProductDto.builder()
                    .productName(longProductName)
                    .productUrl("https://test.com")
                    .price(10000)
                    .category("패딩")
                    .build();
            SsadaguSummaryResponse response = SsadaguSummaryResponse.from(product, "요약");
            List<SsadaguSummaryResponse> summaries = List.of(response);

            given(blogRepository.save(any(Blog.class))).willAnswer(invocation -> {
                Blog savedBlog = invocation.getArgument(0);
                // 제목에 잘린 상품명이 포함되어 있는지 확인
                assertThat(savedBlog.getTitle()).contains("...");
                assertThat(savedBlog.getTitle().length()).isLessThan(200);
                return savedBlog;
            });

            // when
            BlogSaveResult result = blogService.createBlogsFromSummaries(templateId, templateTitle, userId, summaries);

            // then
            assertThat(result.successCount()).isEqualTo(1);
            verify(blogRepository).save(any(Blog.class));
        }

        private SsadaguSummaryResponse createMockSummaryResponse(String category) {
            SsadaguProductDto product = SsadaguProductDto.builder()
                    .productName("테스트 " + category + " 상품")
                    .productUrl("https://ssadagu.kr/test")
                    .price(29900)
                    .rating(4.5)
                    .reviewCount(100)
                    .imageUrl("https://example.com/image.jpg")
                    .category(category)
                    .productAttributes(Map.of("소재", "폴리에스터"))
                    .build();

            return SsadaguSummaryResponse.from(product, "이것은 테스트 AI 요약입니다. " + category + " 상품 추천!");
        }
    }
}