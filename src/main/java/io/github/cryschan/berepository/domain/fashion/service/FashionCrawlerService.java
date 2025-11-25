package io.github.cryschan.berepository.domain.fashion.service;

import io.github.cryschan.berepository.domain.fashion.crawler.MusinsaProductDetailCrawler;
import io.github.cryschan.berepository.domain.fashion.crawler.MusinsaRankingCrawler;
import io.github.cryschan.berepository.domain.fashion.crawler.SsadaguCrawler;
import io.github.cryschan.berepository.domain.fashion.dto.response.MusinsaRankingLinkDto;
import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import io.github.cryschan.berepository.domain.fashion.entity.Product;
import io.github.cryschan.berepository.domain.fashion.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ProductRepository productRepository;

    public List<MusinsaRankingLinkDto> fetchTrendingLinks(Integer limit) {
        int resolvedLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : limit;
        return musinsaRankingCrawler.fetchTopLinks(resolvedLimit);
    }

    /**
     * 전체 크롤링 플로우 실행:
     * 1. 무신사 랭킹에서 상위 N개 상품 URL 가져오기
     * 2. 각 URL에서 카테고리 추출
     * 3. 카테고리로 싸다구에서 상품 검색
     * 4. 상품 정보를 DB에 저장
     *
     * @param limit 크롤링할 상품 수 (기본값: 5)
     * @return 저장된 상품 리스트
     */
    @Transactional
    public List<Product> crawlAndSaveProducts(Integer limit) {
        int resolvedLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : limit;
        List<Product> savedProducts = new ArrayList<>();

        log.info("Starting fashion product crawling flow (limit: {})", resolvedLimit);

        // 1. 무신사 랭킹에서 상위 N개 상품 URL 가져오기
        List<MusinsaRankingLinkDto> musinsaLinks = musinsaRankingCrawler.fetchTopLinks(resolvedLimit);
        log.info("Fetched {} Musinsa ranking links", musinsaLinks.size());

        // 2-4. 각 URL 처리
        for (MusinsaRankingLinkDto link : musinsaLinks) {
            try {
                Product product = processMusinsaProduct(link);
                if (product != null) {
                    savedProducts.add(product);
                }
            } catch (Exception e) {
                log.error("Failed to process Musinsa product: {}", link.href(), e);
                // 하나 실패해도 계속 진행
            }
        }

        log.info("Fashion product crawling completed. Saved {} products", savedProducts.size());
        return savedProducts;
    }

    /**
     * 무신사 상품 하나를 처리하는 메서드
     * - 카테고리 추출
     * - 싸다구 검색
     * - Product 저장
     */
    private Product processMusinsaProduct(MusinsaRankingLinkDto link) {
        String musinsaUrl = link.href();
        log.debug("Processing Musinsa product: {}", musinsaUrl);

        // 중복 체크: 이미 존재하면 기존 상품 반환
        var existingProduct = productRepository.findByMusinsaProductUrl(musinsaUrl);
        if (existingProduct.isPresent()) {
            log.debug("Product already exists: {}, returning existing product", musinsaUrl);
            return existingProduct.get();
        }

        // 2. 카테고리 추출
        String category = musinsaProductDetailCrawler.extractCategory(musinsaUrl);
        if (category == null || category.isBlank()) {
            log.warn("Failed to extract category from Musinsa URL: {}", musinsaUrl);
            return null;
        }
        log.debug("Extracted category: {}", category);

        // 3. 싸다구에서 상품 검색
        SsadaguProductDto ssadaguProduct = ssadaguCrawler.searchFirstProduct(category);
        if (ssadaguProduct == null) {
            log.warn("Failed to find Ssadagu product for category: {}", category);
            return null;
        }
        log.debug("Found Ssadagu product: {}", ssadaguProduct.productName());

        // 4. Product 엔티티 생성 및 저장
        Product product = Product.builder()
                .category(category)
                .musinsaProductUrl(musinsaUrl)
                .ssadaguProductName(ssadaguProduct.productName())
                .ssadaguProductUrl(ssadaguProduct.productUrl())
                .price(ssadaguProduct.price())
                .rating(ssadaguProduct.rating())
                .reviewCount(ssadaguProduct.reviewCount())
                .imageUrl(ssadaguProduct.imageUrl())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Saved product: {} (category: {})", savedProduct.getSsadaguProductName(), savedProduct.getCategory());

        return savedProduct;
    }
}
