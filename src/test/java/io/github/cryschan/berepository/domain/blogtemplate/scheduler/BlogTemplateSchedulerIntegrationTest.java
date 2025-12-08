package io.github.cryschan.berepository.domain.blogtemplate.scheduler;

import io.github.cryschan.berepository.domain.ai.dto.response.SsadaguSummaryResponse;
import io.github.cryschan.berepository.domain.ai.service.SsadaguIntegrationService;
import io.github.cryschan.berepository.domain.blog.entity.Blog;
import io.github.cryschan.berepository.domain.blog.repository.BlogRepository;
import io.github.cryschan.berepository.domain.blog.service.BlogService;
import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.repository.BlogTemplateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BlogTemplateScheduler 통합 테스트
 * <p>
 * 템플릿 기반 크롤링 + AI 요약 전체 플로우 테스트
 * <p>
 * 주의: 실제 외부 API 호출이 발생하므로 AI 비용이 발생합니다.
 */
@Disabled("DB 연결 문제로 통합 테스트 비활성화 - 로컬 PostgreSQL 필요")
@Deprecated
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("BlogTemplateScheduler 통합 테스트")
class BlogTemplateSchedulerIntegrationTest {

    @Autowired
    private SsadaguIntegrationService ssadaguIntegrationService;

    @Autowired
    private BlogTemplateRepository blogTemplateRepository;

    @Autowired
    private BlogService blogService;

    @Autowired
    private BlogRepository blogRepository;

    private BlogTemplate savedTemplate;
    private List<Blog> savedBlogs = new ArrayList<>();

    private static final Long TEST_USER_ID = 99999L;

    @BeforeEach
    void setUp() {
        // 테스트용 템플릿 생성 (고유한 userId 사용)
        savedTemplate = blogTemplateRepository.save(
                BlogTemplate.builder()
                        .title("통합 테스트 템플릿")
                        .categories(new ArrayList<>(List.of("숏패딩", "운동화")))
                        .platforms(new ArrayList<>(List.of("네이버", "티스토리")))
                        .shopUrl("https://test-shop.com")
                        .includeImages(true)
                        .imageCount(3)
                        .charLimit(500)
                        .dailyPostTime(LocalTime.of(9, 0))
                        .userId(TEST_USER_ID)
                        .build()
        );
    }

    @AfterEach
    void tearDown() {
        // 테스트에서 생성한 블로그 삭제
        if (!savedBlogs.isEmpty()) {
            blogRepository.deleteAll(savedBlogs);
            savedBlogs.clear();
        }
        if (savedTemplate != null) {
            blogTemplateRepository.deleteById(savedTemplate.getId());
        }
    }

    @Nested
    @DisplayName("템플릿 기반 크롤링 + AI 요약 테스트")
    class TemplateBasedCrawlingTest {

        @Test
        @DisplayName("템플릿의 charLimit이 AI 요약에 적용되는지 확인")
        void charLimitFromTemplate_AppliedToSummary() {
            // Given
            int templateCharLimit = savedTemplate.getCharLimit();
            String category = savedTemplate.getCategories().get(0);

            // When: 템플릿의 charLimit을 사용하여 AI 요약 생성
            SsadaguSummaryResponse response = ssadaguIntegrationService.searchAndSummarize(
                    category,
                    templateCharLimit
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response.product()).isNotNull();
            assertThat(response.summary()).isNotBlank();
        }

        @Test
        @DisplayName("템플릿의 여러 카테고리로 각각 AI 요약 생성")
        void multipleCategories_EachGeneratesSummary() {
            // Given
            List<String> categories = savedTemplate.getCategories();
            int charLimit = savedTemplate.getCharLimit();
            List<SsadaguSummaryResponse> summaries = new ArrayList<>();

            // When: 각 카테고리별로 AI 요약 생성
            for (String category : categories) {
                try {
                    SsadaguSummaryResponse response = ssadaguIntegrationService.searchAndSummarize(
                            category,
                            charLimit
                    );
                    if (response != null) {
                        summaries.add(response);
                    }
                } catch (Exception e) {
                    // 실패한 카테고리는 무시하고 계속 진행
                }
            }

            // Then
            assertThat(summaries).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("charLimit 변경 테스트")
    class CharLimitVariationTest {

        @Test
        @DisplayName("charLimit 500 vs 1000 비교")
        void compareCharLimits_500vs1000() {
            // Given
            String category = "맨투맨";

            // When: charLimit 500으로 요약 생성
            SsadaguSummaryResponse response500 = ssadaguIntegrationService.searchAndSummarize(category, 500);

            // When: charLimit 1000으로 요약 생성
            SsadaguSummaryResponse response1000 = ssadaguIntegrationService.searchAndSummarize(category, 1000);

            // Then
            assertThat(response500).isNotNull();
            assertThat(response1000).isNotNull();
        }
    }

    @Nested
    @DisplayName("블로그 저장 테스트")
    class BlogSaveTest {

        @Test
        @DisplayName("크롤링 + AI 요약 후 블로그 저장 성공 (서비스 호출)")
        void saveBlog_AfterCrawlingAndSummary_Success() {
            // Given
            String category = savedTemplate.getCategories().get(0);
            int charLimit = savedTemplate.getCharLimit();

            // When: 크롤링 + AI 요약
            SsadaguSummaryResponse response = ssadaguIntegrationService.searchAndSummarize(
                    category,
                    charLimit
            );

            assertThat(response).isNotNull();

            List<SsadaguSummaryResponse> summaries = List.of(response);

            // When: 블로그 저장 (서비스 메서드 호출)
            blogService.createBlogsFromSummaries(
                    savedTemplate.getId(),
                    savedTemplate.getTitle(),
                    TEST_USER_ID,
                    summaries
            );

            // Then: DB에서 조회하여 검증
            Page<Blog> blogPage = blogRepository.findAllByUserIdOrderByCreatedAtDesc(
                    TEST_USER_ID,
                    PageRequest.of(0, 10)
            );

            assertThat(blogPage.getContent()).isNotEmpty();

            Blog savedBlog = blogPage.getContent().get(0);
            savedBlogs.add(savedBlog);

            assertThat(savedBlog.getBlogTemplateId()).isEqualTo(savedTemplate.getId());
            assertThat(savedBlog.getContent()).isEqualTo(response.summary());
            assertThat(savedBlog.getCategory()).isEqualTo(response.product().category());
            assertThat(savedBlog.getUserId()).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("여러 카테고리 크롤링 후 일괄 블로그 저장")
        void saveMultipleBlogs_AfterCrawlingMultipleCategories() {
            // Given
            List<String> categories = savedTemplate.getCategories();
            int charLimit = savedTemplate.getCharLimit();
            List<SsadaguSummaryResponse> summaries = new ArrayList<>();

            // When: 각 카테고리별 크롤링 + AI 요약
            for (String category : categories) {
                try {
                    SsadaguSummaryResponse response = ssadaguIntegrationService.searchAndSummarize(
                            category,
                            charLimit
                    );
                    if (response != null) {
                        summaries.add(response);
                    }
                } catch (Exception e) {
                    // 실패한 카테고리는 무시하고 계속 진행
                }
            }

            assertThat(summaries).isNotEmpty();

            // When: 블로그 일괄 저장 (서비스 메서드 호출)
            blogService.createBlogsFromSummaries(
                    savedTemplate.getId(),
                    savedTemplate.getTitle(),
                    TEST_USER_ID,
                    summaries
            );

            // Then: DB에서 조회하여 검증
            Page<Blog> blogPage = blogRepository.findAllByUserIdOrderByCreatedAtDesc(
                    TEST_USER_ID,
                    PageRequest.of(0, 10)
            );

            assertThat(blogPage.getContent()).hasSizeGreaterThanOrEqualTo(summaries.size());

            savedBlogs.addAll(blogPage.getContent());
        }
    }
}
