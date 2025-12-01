package io.github.cryschan.berepository.domain.ai.service;

import io.github.cryschan.berepository.domain.ai.dto.response.SsadaguSummaryResponse;
import io.github.cryschan.berepository.domain.fashion.crawler.MusinsaProductDetailCrawler;
import io.github.cryschan.berepository.domain.fashion.crawler.MusinsaRankingCrawler;
import io.github.cryschan.berepository.domain.fashion.crawler.SsadaguCrawler;
import io.github.cryschan.berepository.domain.fashion.dto.response.MusinsaRankingLinkDto;
import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 싸다구 상품 검색 + AI 요약 통합 서비스
 * <p>
 * Musinsa 크롤링 → Ssadagu 검색 → AI 요약 전체 플로우를 한 번에 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SsadaguIntegrationService {

    private static final int DEFAULT_CHAR_LIMIT = 200;
    private static final int DEFAULT_LIMIT = 5;

    private final MusinsaRankingCrawler musinsaRankingCrawler;
    private final MusinsaProductDetailCrawler musinsaProductDetailCrawler;
    private final SsadaguCrawler ssadaguCrawler;
    private final SsadaguSummaryService ssadaguSummaryService;

    /**
     * 카테고리로 싸다구 상품 검색 + AI 요약
     *
     * @param category  검색할 카테고리 (예: "숏패딩", "운동화")
     * @param charLimit AI 요약 글자수 제한
     * @return 상품 정보 + AI 요약 응답 (검색 실패 시 null)
     */
    public SsadaguSummaryResponse searchAndSummarize(String category, Integer charLimit) {

        int resolvedCharLimit = (charLimit == null || charLimit <= 0) ? DEFAULT_CHAR_LIMIT : charLimit;

        log.info("Starting search and summarize for category: {}", category);

        // 1. Ssadagu 상품 검색
        SsadaguProductDto product = ssadaguCrawler.searchFirstProduct(category);

        if (product == null) {
            log.warn("No product found for category: {}", category);
            return null;
        }

        log.info("Found product: {} (price: {})", product.productName(), product.price());

        // 2. AI 제목 + 요약 생성
        SsadaguSummaryService.TitleAndSummary result = ssadaguSummaryService.summaryWithTitle(product, resolvedCharLimit);

        log.info("Generated title: {}, summary length: {}", result.title(), result.summary().length());

        return SsadaguSummaryResponse.from(product, result.title(), result.summary());
    }

    /**
     * 무신사 트렌딩 기반 크롤링 + AI 요약
     * <p>
     * 1. 무신사 랭킹에서 상위 N개 상품 URL 가져오기
     * 2. 각 URL에서 카테고리 추출
     * 3. 카테고리로 싸다구에서 상품 검색
     * 4. AI 요약 생성
     *
     * @param limit     크롤링할 상품 수 (기본값: 5)
     * @param charLimit AI 요약 글자수 제한 (기본값: 200)
     * @return 상품 정보 + AI 요약 응답 리스트
     */
    public List<SsadaguSummaryResponse> crawlAndSummarizeFromTrending(Integer limit, Integer charLimit) {

        int resolvedLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : limit;
        int resolvedCharLimit = (charLimit == null || charLimit <= 0) ? DEFAULT_CHAR_LIMIT : charLimit;

        List<SsadaguSummaryResponse> results = new ArrayList<>();

        log.info("Starting crawl and summarize from trending (limit: {}, charLimit: {})", resolvedLimit, resolvedCharLimit);

        // 1. 무신사 랭킹에서 상위 N개 상품 URL 가져오기
        List<MusinsaRankingLinkDto> rankingLinks = musinsaRankingCrawler.fetchTopLinks(resolvedLimit);
        log.info("Fetched {} Musinsa ranking links", rankingLinks.size());

        // 2-4. 각 URL 처리
        for (MusinsaRankingLinkDto link : rankingLinks) {
            try {
                String musinsaUrl = link.href();
                log.debug("Processing Musinsa product (rank {}): {}", link.rank(), musinsaUrl);

                // 카테고리 추출
                String category = musinsaProductDetailCrawler.extractCategory(musinsaUrl);
                if (category == null || category.isBlank()) {
                    log.warn("Failed to extract category from: {}", musinsaUrl);
                    continue;
                }

                // Ssadagu 검색 + AI 요약
                SsadaguSummaryResponse response = searchAndSummarize(category, resolvedCharLimit);
                if (response != null) {
                    results.add(response);
                    log.info("Added summary for category: {} (product: {})", category, response.product().productName());
                }

            } catch (Exception e) {
                log.error("Failed to process Musinsa product: {}", link.href(), e);
            }
        }

        log.info("Crawl and summarize completed. Generated {} summaries", results.size());
        return results;
    }
}
