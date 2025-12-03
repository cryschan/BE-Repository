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