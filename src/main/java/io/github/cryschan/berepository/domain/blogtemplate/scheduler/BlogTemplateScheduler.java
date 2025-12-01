package io.github.cryschan.berepository.domain.blogtemplate.scheduler;

import io.github.cryschan.berepository.domain.ai.dto.response.SsadaguSummaryResponse;
import io.github.cryschan.berepository.domain.ai.service.SsadaguIntegrationService;
import io.github.cryschan.berepository.domain.blog.dto.response.BlogSaveResult;
import io.github.cryschan.berepository.domain.blog.service.BlogService;
import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.service.BlogTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlogTemplateScheduler {

    private final BlogTemplateService blogTemplateService;
    private final SsadaguIntegrationService ssadaguIntegrationService;
    private final BlogService blogService;

    @Scheduled(cron = "1 * * * * *")
    public void collectTemplatesForCurrentSlot() {
        LocalTime currentSlot = LocalTime.now().withSecond(0).withNano(0);
        log.info("========== 스케줄러 실행: {} ==========", currentSlot);

        List<BlogTemplate> templates = blogTemplateService.getTemplatesForTime(currentSlot);
        log.info("현재 시간 {}에 예약된 블로그 템플릿 {} 개 발견", currentSlot, templates.size());

        if (templates.isEmpty()) {
            log.info("처리할 템플릿이 없습니다. 스케줄러 종료.");
            return;
        }

        // 각 템플릿에 대해 크롤링 및 블로그 글 생성 처리
        for (BlogTemplate template : templates) {
            try {
                processTemplate(template);
            } catch (Exception e) {
                log.error("사용자 {} 템플릿 처리 실패: {}", template.getUserId(), e.getMessage(), e);
                // 하나의 템플릿 처리 실패해도 다른 템플릿은 계속 처리
            }
        }
    }

    /**
     * 템플릿 기반으로 크롤링 + AI 요약 처리
     */
    private void processTemplate(BlogTemplate template) {
        log.info("사용자 {} 템플릿 처리 시작 (제목: {})", template.getUserId(), template.getTitle());

        List<String> categories = template.getCategories();
        if (categories == null || categories.isEmpty()) {
            log.warn("템플릿 {}에 카테고리가 없습니다. 건너뜁니다", template.getId());
            return;
        }

        int charLimit = template.getCharLimit();
        log.info("템플릿 카테고리: {}, 글자 제한: {}", categories, charLimit);

        List<SsadaguSummaryResponse> summaries = new ArrayList<>();

        log.info("========== 크롤링 + AI 요약 시작 ==========");
        log.info("처리할 카테고리 수: {}", categories.size());

        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            log.info("[{}/{}] 카테고리 '{}' 처리 시작", i + 1, categories.size(), category);

            try {
                log.debug("  → 싸다구 크롤링 시작: {}", category);
                long startTime = System.currentTimeMillis();

                SsadaguSummaryResponse response =
                        ssadaguIntegrationService.searchAndSummarize(category, charLimit);

                long elapsed = System.currentTimeMillis() - startTime;

                if (response != null) {
                    summaries.add(response);
                    log.info("[{}/{}] 카테고리 '{}' 처리 완료 ({}ms)", i + 1, categories.size(), category, elapsed);
                    log.info("  → 상품명: {}", response.product().productName());
                    log.info("  → 가격: {}원", response.product().price());
                    log.info("  → 요약 길이: {}자 (제한: {}자)", response.summary().length(), charLimit);
                    log.debug("  → 요약 내용: {}", response.summary());
                } else {
                    log.warn("[{}/{}] 카테고리 '{}' - 검색 결과 없음 ({}ms)", i + 1, categories.size(), category, elapsed);
                }
            } catch (Exception e) {
                log.error("[{}/{}] 카테고리 '{}' 처리 실패: {}", i + 1, categories.size(), category, e.getMessage(), e);
            }
        }

        log.info("========== 크롤링 + AI 요약 완료 ==========");
        log.info("성공: {}/{} 카테고리", summaries.size(), categories.size());

        if (summaries.isEmpty()) {
            log.warn("템플릿 {}에 대해 생성된 요약이 없습니다", template.getId());
            return;
        }

        log.info("템플릿 {} 처리 완료 - 사용자: {}, 요약 수: {}, 글자 제한: {}",
                template.getId(),
                template.getUserId(),
                summaries.size(),
                charLimit);

        // 블로그 글 저장 (서비스에 위임) - 4번: 필요한 값만 전달하여 순환 의존성 해결
        BlogSaveResult result = blogService.createBlogsFromSummaries(
                template.getId(),
                template.getTitle(),
                template.getUserId(),
                summaries
        );

        if (!result.isAllSuccess()) {
            log.warn("일부 블로그 저장 실패 - 성공: {}, 실패: {}", result.successCount(), result.failCount());
        }
    }
}

