package io.github.cryschan.berepository.domain.blogtemplate.repository;

import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@Disabled("DB 연결 문제로 Repository 테스트 비활성화 - 로컬 PostgreSQL 필요")
@Deprecated
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("BlogTemplateRepository 테스트")
class BlogTemplateRepositoryTest {

    @Autowired
    private BlogTemplateRepository blogTemplateRepository;

    @Autowired
    private EntityManager entityManager;

    private BlogTemplate savedTemplate;
    private LocalTime testPostTime;
    private static final AtomicLong USER_ID_GENERATOR = new AtomicLong(100000L);
    private static final AtomicLong TIME_GENERATOR = new AtomicLong(0L);

    @BeforeEach
    void setUp() {
        // 각 테스트마다 고유한 시간 사용 (00:00 ~ 23:59 범위)
        long timeValue = TIME_GENERATOR.incrementAndGet();
        testPostTime = LocalTime.of((int) (timeValue % 24), (int) (timeValue % 60));

        savedTemplate = blogTemplateRepository.save(
                BlogTemplate.builder()
                        .title("테스트 템플릿")
                        .categories(new ArrayList<>(List.of("패딩", "운동화", "맨투맨")))
                        .platforms(new ArrayList<>(List.of("네이버", "티스토리")))
                        .shopUrl("https://test-shop.com")
                        .includeImages(true)
                        .imageCount(3)
                        .charLimit(500)
                        .dailyPostTime(testPostTime)
                        .userId(USER_ID_GENERATOR.incrementAndGet())
                        .build()
        );
        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("findByDailyPostTimeWithCollections 테스트")
    class FindByDailyPostTimeWithCollectionsTest {

        @Test
        @DisplayName("Fetch Join으로 categories가 함께 로드되어 세션 종료 후에도 접근 가능")
        void fetchJoinLoadsCategories_NoLazyInitializationException() {
            // When: Fetch Join 쿼리로 조회
            List<BlogTemplate> templates = blogTemplateRepository
                    .findByDailyPostTimeWithCollections(testPostTime);

            // Then: 영속성 컨텍스트 완전히 종료
            entityManager.clear();

            // 세션 종료 후에도 categories 접근 가능해야 함
            assertThat(templates).hasSize(1);
            BlogTemplate template = templates.get(0);

            assertThatNoException()
                    .isThrownBy(() -> {
                        List<String> categories = template.getCategories();
                        assertThat(categories).containsExactlyInAnyOrder("패딩", "운동화", "맨투맨");
                    });
        }

        @Test
        @DisplayName("해당 시간에 예약된 템플릿이 없으면 빈 리스트 반환")
        void noTemplatesForTime_ReturnsEmptyList() {
            // Given
            LocalTime differentTime = LocalTime.of(10, 0);

            // When
            List<BlogTemplate> templates = blogTemplateRepository
                    .findByDailyPostTimeWithCollections(differentTime);

            // Then
            assertThat(templates).isEmpty();
        }

        @Test
        @DisplayName("여러 템플릿이 같은 시간에 예약되어 있으면 모두 조회")
        void multipleTemplatesSameTime_ReturnsAll() {
            // Given: 같은 시간에 두 번째 템플릿 추가
            blogTemplateRepository.save(
                    BlogTemplate.builder()
                            .title("두 번째 템플릿")
                            .categories(new ArrayList<>(List.of("구두")))
                            .platforms(new ArrayList<>(List.of("블로그")))
                            .shopUrl("https://test2.com")
                            .includeImages(false)
                            .imageCount(0)
                            .charLimit(1000)
                            .dailyPostTime(testPostTime)
                            .userId(USER_ID_GENERATOR.incrementAndGet())
                            .build()
            );
            entityManager.flush();
            entityManager.clear();

            // When
            List<BlogTemplate> templates = blogTemplateRepository
                    .findByDailyPostTimeWithCollections(testPostTime);

            // Then
            assertThat(templates).hasSize(2);

            // 각 템플릿의 categories에 접근 가능
            for (BlogTemplate template : templates) {
                assertThatNoException()
                        .isThrownBy(() -> template.getCategories().size());
            }
        }

        @Test
        @DisplayName("categories가 비어있어도 템플릿 조회 가능")
        void emptyCategories_StillReturnsTemplate() {
            // Given
            LocalTime emptyTime = LocalTime.of(11, 0);
            blogTemplateRepository.save(
                    BlogTemplate.builder()
                            .title("빈 카테고리 템플릿")
                            .categories(new ArrayList<>())
                            .platforms(new ArrayList<>())
                            .shopUrl("https://empty.com")
                            .includeImages(false)
                            .imageCount(0)
                            .charLimit(500)
                            .dailyPostTime(emptyTime)
                            .userId(USER_ID_GENERATOR.incrementAndGet())
                            .build()
            );
            entityManager.flush();
            entityManager.clear();

            // When
            List<BlogTemplate> templates = blogTemplateRepository
                    .findByDailyPostTimeWithCollections(emptyTime);

            // Then
            assertThat(templates).hasSize(1);
            assertThat(templates.get(0).getCategories()).isEmpty();
        }
    }
}
