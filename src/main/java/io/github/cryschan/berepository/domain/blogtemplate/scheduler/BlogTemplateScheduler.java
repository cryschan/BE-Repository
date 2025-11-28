package io.github.cryschan.berepository.domain.blogtemplate.scheduler;

import io.github.cryschan.berepository.domain.blogtemplate.dto.BlogContentGenerationRequest;
import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.service.BlogTemplateService;
import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import io.github.cryschan.berepository.domain.fashion.service.FashionCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlogTemplateScheduler {

    private final BlogTemplateService blogTemplateService;
    private final FashionCrawlerService fashionCrawlerService;

    @Scheduled(cron = "0 * * * * *")
    public void collectTemplatesForCurrentSlot() {
        LocalTime currentSlot = LocalTime.now().withSecond(0).withNano(0);
        List<BlogTemplate> templates = blogTemplateService.getTemplatesForTime(currentSlot);
        if (templates.isEmpty()) {
            return;
        }

        log.info("현재 시간 {}에 예약된 블로그 템플릿 {} 개 발견", currentSlot, templates.size());

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
     * 템플릿 기반으로 크롤링 및 블로그 글 생성 처리
     */
    private void processTemplate(BlogTemplate template) {
        log.info("사용자 {} 템플릿 처리 시작 (제목: {})", template.getUserId(), template.getTitle());

        List<String> categories = template.getCategories();
        if (categories == null || categories.isEmpty()) {
            log.warn("템플릿 {}에 카테고리가 없습니다. 건너뜁니다", template.getId());
            return;
        }

        log.info("템플릿 카테고리: {}", categories);

        // 각 카테고리별로 상품 크롤링
        List<SsadaguProductDto> crawledProducts = fashionCrawlerService.crawlProductsByCategories(categories);

        if (crawledProducts.isEmpty()) {
            log.warn("템플릿 {}에 대해 크롤링된 상품이 없습니다. 블로그 생성을 건너뜁니다", template.getId());
            return;
        }

        log.info("템플릿 {}에 대해 {} 개 상품 크롤링 완료", template.getId(), crawledProducts.size());

        // 템플릿 설정과 크롤링 결과를 합쳐서 블로그 컨텐츠 생성 요청 DTO 생성
        BlogContentGenerationRequest generationRequest = BlogContentGenerationRequest.builder()
                .userId(template.getUserId())
                .templateTitle(template.getTitle())
                .charLimit(template.getCharLimit())
                .includeImages(template.isIncludeImages())
                .imageCount(template.getImageCount())
                .platforms(template.getPlatforms())
                .crawledProducts(crawledProducts)
                .build();

        log.info("블로그 컨텐츠 생성 요청 생성 완료 - 사용자: {}, 상품 수: {}, 글자 제한: {}, 이미지 포함: {}, 이미지 개수: {}, 플랫폼: {}",
                generationRequest.userId(),
                generationRequest.crawledProducts().size(),
                generationRequest.charLimit(),
                generationRequest.includeImages(),
                generationRequest.imageCount(),
                generationRequest.platforms());

        // TODO: AI 블로그 글 생성 및 발행 서비스 호출
        // Example: blogContentService.generateAndPublish(generationRequest);
        log.info("TODO: AI 블로그 글 생성 서비스 호출 예정");
    }
}

