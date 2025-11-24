package io.github.cryschan.berepository._global;

import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.repository.BlogTemplateRepository;
import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

/**
 * 애플리케이션 시작 시 초기 블로그 템플릿 데이터를 생성하는 클래스
 * User가 존재하지 않으면 에러가 발생합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BlogTemplateInitializer implements CommandLineRunner {

    private final BlogTemplateRepository blogTemplateRepository;
    private final UserRepository userRepository;

    @Value("${init.admin.email}")
    private String adminEmail;

    @Value("${init.user.email}")
    private String userEmail;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=".repeat(80));
        log.info("Starting Initial BlogTemplate Data Initialization...");
        log.info("=".repeat(80));

        try {
            createAdminTemplateIfNotExists();
            createUserTemplateIfNotExists();

            log.info("=".repeat(80));
            log.info("Initial BlogTemplate Data Initialization Completed Successfully");
            log.info("=".repeat(80));
        } catch (Exception e) {
            log.error("=".repeat(80));
            log.error("Initial BlogTemplate Data Initialization Failed!", e);
            log.error("=".repeat(80));
            throw e;
        }
    }

    private void createAdminTemplateIfNotExists() {
        log.debug("Checking if Admin's BlogTemplate exists...");

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalStateException("Admin user must exist before creating template"));

        if (blogTemplateRepository.findByUserId(admin.getUserId()).isPresent()) {
            log.info("[SKIP] Admin's BlogTemplate already exists");
            return;
        }

        log.debug("Admin's BlogTemplate does not exist. Creating...");

        BlogTemplate adminTemplate = BlogTemplate.builder()
                .userId(admin.getUserId())
                .title("System Admin의 템플릿")
                .categories(List.of("상의", "하의", "아우터"))
                .platforms(List.of("Tistory"))
                .shopUrl("https://www.musinsa.com")
                .includeImages(true)
                .imageCount(3)
                .charLimit(1000)
                .dailyPostTime(LocalTime.of(9, 0, 0))
                .build();

        BlogTemplate saved = blogTemplateRepository.save(adminTemplate);

        log.info("[CREATED] Admin's BlogTemplate successfully created");
        log.debug("  - Template ID: {}", saved.getId());
        log.debug("  - User ID: {}", saved.getUserId());
        log.debug("  - Title: {}", saved.getTitle());
        log.debug("  - Categories: {}", saved.getCategories());
        log.debug("  - Platforms: {}", saved.getPlatforms());
        log.debug("  - Daily Post Time: {}", saved.getDailyPostTime());
    }

    private void createUserTemplateIfNotExists() {
        log.debug("Checking if Regular User's BlogTemplate exists...");

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Regular user must exist before creating template"));

        if (blogTemplateRepository.findByUserId(user.getUserId()).isPresent()) {
            log.info("[SKIP] Regular User's BlogTemplate already exists");
            return;
        }

        log.debug("Regular User's BlogTemplate does not exist. Creating...");

        BlogTemplate userTemplate = BlogTemplate.builder()
                .userId(user.getUserId())
                .title("Test User의 템플릿")
                .categories(List.of("신발", "ACC"))
                .platforms(List.of("Naver"))
                .shopUrl("https://www.musinsa.com/app/")
                .includeImages(true)
                .imageCount(5)
                .charLimit(1500)
                .dailyPostTime(LocalTime.of(14, 0, 0))
                .build();

        BlogTemplate saved = blogTemplateRepository.save(userTemplate);

        log.info("[CREATED] Regular User's BlogTemplate successfully created");
        log.debug("  - Template ID: {}", saved.getId());
        log.debug("  - User ID: {}", saved.getUserId());
        log.debug("  - Title: {}", saved.getTitle());
        log.debug("  - Categories: {}", saved.getCategories());
        log.debug("  - Platforms: {}", saved.getPlatforms());
        log.debug("  - Daily Post Time: {}", saved.getDailyPostTime());
    }
}
