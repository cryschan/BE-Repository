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
                "https://example.com/images/tshirt.jpg",
                "# 여름 남성 반팔 티셔츠 추천\n\n여름철 필수 아이템인 반팔 티셔츠를 소개합니다...",
                "남성 의류",
                1L
        );

        // 2. 메이크업 초보자를 위한 베이스 메이크업 제품 가이드
        createBlog(
                1L,
                "메이크업 초보자를 위한 베이스 메이크업 제품 가이드",
                "https://example.com/images/makeup.jpg",
                "# 베이스 메이크업 가이드\n\n초보자도 쉽게 따라할 수 있는 베이스 메이크업...",
                "메이크업 제품",
                2L
        );

        // 3. 편안한 운동화 추천
        createBlog(
                2L,
                "편안한 운동화 추천 - 일상에서 신기 좋은",
                "https://example.com/images/sneakers.jpg",
                "# 운동화 추천\n\n일상생활에서 편하게 신을 수 있는 운동화를 추천합니다...",
                "신발",
                1L
        );

        // 4. 겨울 패딩 추천
        createBlog(
                1L,
                "겨울 패딩 추천 - 따뜻하고 가벼운",
                "https://example.com/images/padding.jpg",
                "# 겨울 패딩 추천\n\n가볍고 따뜻한 겨울 패딩을 소개합니다...",
                "여성 의류",
                2L
        );

        // 5. 스킨케어 루틴 완벽 가이드
        createBlog(
                2L,
                "스킨케어 루틴 완벽 가이드",
                "https://example.com/images/skincare.jpg",
                "# 스킨케어 루틴\n\n올바른 스킨케어 순서와 제품 추천...",
                "메이크업 제품",
                1L
        );

        // 6. 홈 인테리어 소품 추천
        createBlog(
                1L,
                "홈 인테리어 소품 추천",
                "https://example.com/images/interior.jpg",
                "# 인테리어 소품\n\n집을 더 아늑하게 만들어줄 소품들...",
                "생활용품",
                2L
        );

        // 7. 최신 노트북 비교 리뷰
        createBlog(
                2L,
                "최신 노트북 비교 리뷰",
                "https://example.com/images/laptop.jpg",
                "# 노트북 리뷰\n\n2024년 최신 노트북 비교 분석...",
                "전자제품",
                1L
        );

        // 8. 건강한 간식 추천
        createBlog(
                1L,
                "건강한 간식 추천",
                "https://example.com/images/snack.jpg",
                "# 건강 간식\n\n맛있고 건강한 간식 추천...",
                "식품",
                2L
        );

        // 9. 남성 액세서리 추천
        createBlog(
                2L,
                "남성 액세서리 추천 - 시계와 팔찌",
                "https://example.com/images/accessory-men.jpg",
                "# 남성 액세서리\n\n스타일을 완성하는 액세서리...",
                "액세서리",
                1L
        );

        // 10. 여성 가방 추천
        createBlog(
                1L,
                "여성 가방 추천 - 실용적이고 예쁜",
                "https://example.com/images/bag.jpg",
                "# 여성 가방\n\n실용성과 디자인을 모두 갖춘 가방...",
                "액세서리",
                2L
        );

        // 11. 주방 용품 추천
        createBlog(
                2L,
                "주방 용품 추천 - 요리가 즐거워지는",
                "https://example.com/images/kitchen.jpg",
                "# 주방 용품\n\n요리를 더 편하게 만들어줄 용품들...",
                "생활용품",
                1L
        );

        // 12. 무선 이어폰 추천
        createBlog(
                1L,
                "무선 이어폰 추천 - 음질과 편의성",
                "https://example.com/images/earbuds.jpg",
                "# 무선 이어폰\n\n최고의 무선 이어폰 추천...",
                "전자제품",
                2L
        );

        log.info("[CREATED] Total {} blogs created successfully", blogRepository.count());
    }

    private void createBlog(Long templateId, String title, String imgUrl,
                            String content, String category, Long userId) {
        log.debug("Creating blog: {}", title);

        Blog blog = Blog.builder()
                .blogTemplateId(templateId)
                .title(title)
                .imgUrl(imgUrl)
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