package io.github.cryschan.berepository.domain.fashion.service;

import io.github.cryschan.berepository.domain.blogtemplate.dto.BlogContentGenerationRequest;
import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.exception.BlogTemplateException;
import io.github.cryschan.berepository.domain.blogtemplate.repository.BlogTemplateRepository;
import io.github.cryschan.berepository.domain.fashion.crawler.MusinsaProductDetailCrawler;
import io.github.cryschan.berepository.domain.fashion.crawler.MusinsaRankingCrawler;
import io.github.cryschan.berepository.domain.fashion.crawler.SsadaguCrawler;
import io.github.cryschan.berepository.domain.fashion.dto.response.MusinsaRankingLinkDto;
import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import io.github.cryschan.berepository.domain.fashion.exception.FashionException;
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
    private final BlogTemplateRepository blogTemplateRepository;

    /**
     * 무신사 랭킹에서 카테고리를 추출하고, 싸다구에서 상품을 검색
     * (DB 저장 없이 크롤링만 수행)
     *
     * @param category 검색할 카테고리 (예: "패딩", "구두")
     * @return 싸다구 상품 정보
     * @throws FashionException 카테고리가 없거나, 상품을 찾지 못한 경우
     */
    public SsadaguProductDto searchProductByCategory(String category) {
        if (category == null || category.isBlank()) {
            log.warn("카테고리가 비어있습니다");
            throw FashionException.categoryRequired();
        }

        log.info("카테고리로 상품 검색 중: {}", category);

        // 싸다구에서 상품 검색
        SsadaguProductDto product = ssadaguCrawler.searchFirstProduct(category);

        if (product == null) {
            log.warn("'{}' 카테고리에서 상품을 찾지 못했습니다", category);
            throw FashionException.noProductFound(category);
        }

        log.info("상품 검색 성공: {} (카테고리: {})", product.productName(), category);
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

        log.info("패션 상품 크롤링 완료. 총 {} 개 상품 발견", products.size());
        return products;
    }

    /**
     * 여러 카테고리로 상품을 크롤링하고 BlogContentGenerationRequest를 위한 데이터 수집
     * (내부용: Scheduler에서 사용)
     *
     * @param categories 검색할 카테고리 목록
     * @return 크롤링된 상품 정보 리스트
     */
    public List<SsadaguProductDto> crawlProductsByCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            log.warn("카테고리 목록이 비어있습니다");
            return List.of();
        }

        List<SsadaguProductDto> products = new ArrayList<>();
        log.info("카테고리 기반 크롤링 시작: {} 개 카테고리", categories.size());

        for (String category : categories) {
            try {
                log.debug("카테고리 처리 중: {}", category);
                SsadaguProductDto product = ssadaguCrawler.searchFirstProduct(category);

                if (product != null) {
                    products.add(product);
                    log.info("상품 수집 성공: {} (카테고리: {})", product.productName(), category);
                } else {
                    log.warn("'{}' 카테고리에서 상품을 찾지 못했습니다", category);
                }
            } catch (Exception e) {
                log.error("카테고리 '{}' 크롤링 중 오류 발생: {}", category, e.getMessage(), e);
                // 하나의 카테고리 실패해도 계속 진행
            }
        }

        log.info("크롤링 완료. 총 {} 개 상품 수집", products.size());
        return products;
    }

    /**
     * 사용자 템플릿 기반 상품 크롤링 및 블로그 생성 요청 데이터 생성
     * 1. 사용자의 블로그 템플릿 조회
     * 2. 템플릿의 카테고리로 상품 크롤링
     * 3. 템플릿 설정과 크롤링 결과를 합쳐서 BlogContentGenerationRequest 생성
     *
     * @param userId 사용자 ID
     * @return 블로그 생성 요청 데이터
     * @throws BlogTemplateException 블로그 템플릿을 찾을 수 없는 경우
     */
    public BlogContentGenerationRequest generateBlogContentRequest(Long userId) {
        log.info("사용자 {}의 템플릿 기반 크롤링 시작", userId);

        // 1. 사용자의 블로그 템플릿 조회
        BlogTemplate template = blogTemplateRepository.findByUserId(userId)
                .orElseThrow(() -> BlogTemplateException.notFoundByUserId(userId));
        log.debug("템플릿 조회 성공: {} (카테고리: {})", template.getTitle(), template.getCategories());

        // 2. 템플릿의 카테고리로 상품 크롤링
        List<SsadaguProductDto> crawledProducts = crawlProductsByCategories(template.getCategories());
        log.info("총 {} 개 상품 크롤링 완료", crawledProducts.size());

        // 3. 템플릿 설정과 크롤링 결과를 합쳐서 BlogContentGenerationRequest 생성
        BlogContentGenerationRequest request = BlogContentGenerationRequest.builder()
                .userId(template.getUserId())
                .templateTitle(template.getTitle())
                .charLimit(template.getCharLimit())
                .includeImages(template.isIncludeImages())
                .imageCount(template.getImageCount())
                .platforms(template.getPlatforms())
                .crawledProducts(crawledProducts)
                .build();

        log.info("블로그 생성 요청 데이터 생성 완료: userId={}, products={}", userId, crawledProducts.size());
        return request;
    }
}
