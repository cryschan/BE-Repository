package io.github.cryschan.berepository._global;

import io.github.cryschan.berepository.domain.blog.entity.Blog;
import io.github.cryschan.berepository.domain.blog.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 시작 시 초기 블로그 데이터를 생성하는 클래스
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Order(3)
public class BlogInitializer implements CommandLineRunner {

    private final BlogRepository blogRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=".repeat(80));
        log.info("Starting Initial Blog Data Initialization...");
        log.info("=".repeat(80));

        try {
            createBlogsIfNotExists();

            log.info("=".repeat(80));
            log.info("Initial Blog Data Initialization Completed Successfully");
            log.info("=".repeat(80));
        } catch (Exception e) {
            log.error("=".repeat(80));
            log.error("Initial Blog Data Initialization Failed!", e);
            log.error("=".repeat(80));
            throw e;
        }
    }

    private void createBlogsIfNotExists() {
        log.debug("Checking if Blog data exists...");

        if (blogRepository.count() > 0) {
            log.info("[SKIP] Blog data already exists. Count: {}", blogRepository.count());
            return;
        }

        log.debug("Blog data does not exist. Creating initial blogs...");

        // 1. 여름 남성 반팔 티셔츠 추천
        createBlog(
                1L,
                "여름 남성 반팔 티셔츠 추천 - 시원하고 스타일리시한",
                "# 여름 남성 반팔 티셔츠 추천\n\n여름철 필수 아이템인 반팔 티셔츠를 소개합니다...",
                "상의",
                1L
        );

        // 2. 메이크업 초보자를 위한 베이스 메이크업 제품 가이드
        createBlog(
                1L,
                "메이크업 초보자를 위한 베이스 메이크업 제품 가이드",
                "# 베이스 메이크업 가이드\n\n초보자도 쉽게 따라할 수 있는 베이스 메이크업...",
                "ACC",
                2L
        );

        // 3. 편안한 운동화 추천
        createBlog(
                2L,
                "편안한 운동화 추천 - 일상에서 신기 좋은",
                "# 운동화 추천\n\n일상생활에서 편하게 신을 수 있는 운동화를 추천합니다...",
                "신발",
                1L
        );

        // 4. 겨울 패딩 추천
        createBlog(
                1L,
                "겨울 패딩 추천 - 따뜻하고 가벼운",
                "# 겨울 패딩 추천\n\n가볍고 따뜻한 겨울 패딩을 소개합니다...",
                "아우터",
                2L
        );

        // 5. 스킨케어 루틴 완벽 가이드
        createBlog(
                2L,
                "스킨케어 루틴 완벽 가이드",
                "# 스킨케어 루틴\n\n올바른 스킨케어 순서와 제품 추천...",
                "ACC",
                1L
        );

        // 6. 데님 팬츠 스타일링 가이드
        createBlog(
                1L,
                "데님 팬츠 스타일링 가이드",
                "# 데님 팬츠 스타일링\n\n다양한 데님 팬츠 코디법을 소개합니다...",
                "하의",
                2L
        );

        // 7. 가을 자켓 추천
        createBlog(
                2L,
                "가을 자켓 추천 - 트렌디한 아우터",
                "# 가을 자켓 추천\n\n2024년 가을 트렌드 자켓을 소개합니다...",
                "아우터",
                1L
        );

        // 8. 여름 린넨 셔츠 추천
        createBlog(
                1L,
                "여름 린넨 셔츠 추천",
                "# 린넨 셔츠\n\n시원한 여름을 위한 린넨 셔츠...",
                "상의",
                2L
        );

        // 9. 남성 액세서리 추천
        createBlog(
                2L,
                "남성 액세서리 추천 - 시계와 팔찌",
                "# 남성 액세서리\n\n스타일을 완성하는 액세서리...",
                "ACC",
                1L
        );

        // 10. 여성 슬랙스 추천
        createBlog(
                1L,
                "여성 슬랙스 추천 - 오피스룩 필수템",
                "# 여성 슬랙스\n\n오피스룩에 어울리는 슬랙스 추천...",
                "하의",
                2L
        );

        // 11. 로퍼 스타일링 가이드
        createBlog(
                2L,
                "로퍼 스타일링 가이드",
                "# 로퍼 스타일링\n\n클래식한 로퍼 코디법...",
                "신발",
                1L
        );

        // 12. 겨울 니트 추천
        createBlog(
                1L,
                "겨울 니트 추천 - 따뜻하고 포근한",
                "# 겨울 니트\n\n포근한 겨울 니트 추천...",
                "상의",
                2L
        );

        log.info("[CREATED] Total {} blogs created successfully", blogRepository.count());
    }

    private void createBlog(Long templateId, String title, String content, String category, Long userId) {
        log.debug("Creating blog: {}", title);

        Blog blog = Blog.builder()
                .blogTemplateId(templateId)
                .title(title)
                .content(content)
                .category(category)
                .userId(userId)
                .build();

        Blog savedBlog = blogRepository.save(blog);

        log.info("[CREATED] Blog successfully created");
        log.debug("  - Blog ID: {}", savedBlog.getId());
        log.debug("  - Title: {}", savedBlog.getTitle());
        log.debug("  - Category: {}", savedBlog.getCategory());
        log.debug("  - Template ID: {}", savedBlog.getBlogTemplateId());
        log.debug("  - User ID: {}", savedBlog.getUserId());
    }
}