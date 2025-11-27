package io.github.cryschan.berepository.domain.fashion.service;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class FashionCrawlerService {

    private static final int DEFAULT_LIMIT = 5;

    private final MusinsaRankingCrawler musinsaRankingCrawler;
    private final MusinsaProductDetailCrawler musinsaProductDetailCrawler;
    private final SsadaguCrawler ssadaguCrawler;

    /**
     * 무신사 랭킹에서 카테고리를 추출하고, 싸다구에서 상품을 검색
     * (DB 저장 없이 크롤링만 수행)
     *
     * @param category 검색할 카테고리 (예: "패딩", "구두")
     * @return 싸다구 상품 정보 (null 가능)
     */
    public SsadaguProductDto searchProductByCategory(String category) {
        if (category == null || category.isBlank()) {
            log.warn("Category is null or empty");
            return null;
        }

        log.info("Searching product for category: {}", category);

        // 싸다구에서 상품 검색
        SsadaguProductDto product = ssadaguCrawler.searchFirstProduct(category);

        if (product == null) {
            log.warn("Failed to find Ssadagu product for category: {}", category);
            return null;
        }

        log.info("Found Ssadagu product: {} (category: {})", product.productName(), category);
        return product;
    }

    /**
     * 무신사 랭킹 기반 크롤링 플로우
     * 1. 무신사 랭킹에서 상위 N개 상품 URL 가져오기
     * 2. 각 URL에서 카테고리 추출
     * 3. 카테고리로 싸다구에서 상품 검색
     *
     * @param limit 크롤링할 상품 수 (기본값: 5)
     * @return 크롤링된 상품 정보 리스트
     */
    public List<SsadaguProductDto> crawlProductsFromTrending(Integer limit) {
        int resolvedLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : limit;
        List<SsadaguProductDto> products = new ArrayList<>();

        log.info("Starting fashion product crawling flow (limit: {})", resolvedLimit);

        // 1. 무신사 랭킹에서 상위 N개 상품 URL 가져오기
        List<MusinsaRankingLinkDto> musinsaLinks = musinsaRankingCrawler.fetchTopLinks(resolvedLimit);
        log.info("Fetched {} Musinsa ranking links", musinsaLinks.size());

        // 2-3. 각 URL 처리
        for (MusinsaRankingLinkDto link : musinsaLinks) {
            try {
                String musinsaUrl = link.href();
                log.debug("Processing Musinsa product: {}", musinsaUrl);

                // 카테고리 추출
                String category = musinsaProductDetailCrawler.extractCategory(musinsaUrl);
                if (category == null || category.isBlank()) {
                    log.warn("Failed to extract category from Musinsa URL: {}", musinsaUrl);
                    continue;
                }
                log.debug("Extracted category: {}", category);

                // 싸다구에서 상품 검색
                SsadaguProductDto product = ssadaguCrawler.searchFirstProduct(category);
                if (product != null) {
                    products.add(product);
                    log.info("Found product: {} (category: {})", product.productName(), category);
                }
            } catch (Exception e) {
                log.error("Failed to process Musinsa product: {}", link.href(), e);
                // 하나 실패해도 계속 진행
            }
        }

        log.info("Fashion product crawling completed. Found {} products", products.size());
        return products;
    }
}
