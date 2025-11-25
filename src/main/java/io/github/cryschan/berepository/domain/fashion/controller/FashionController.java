package io.github.cryschan.berepository.domain.fashion.controller;

import io.github.cryschan.berepository.domain.fashion.dto.response.KeywordResponseDto;
import io.github.cryschan.berepository.domain.fashion.dto.response.MusinsaRankingLinkDto;
import io.github.cryschan.berepository.domain.fashion.dto.response.ProductResponseDto;
import io.github.cryschan.berepository.domain.fashion.entity.Product;
import io.github.cryschan.berepository.domain.fashion.repository.ProductRepository;
import io.github.cryschan.berepository.domain.fashion.service.FashionCrawlerService;
import io.github.cryschan.berepository.domain.fashion.service.MagazineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Fashion", description = "패션 상품 크롤링 API (로그인 필수)")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
@RequestMapping("/api/keyword")
@RestController
public class FashionController {

    private final MagazineService magazineService;
    private final FashionCrawlerService fashionCrawlerService;
    private final ProductRepository productRepository;

    @Operation(
            summary = "무신사 매거진 인기 키워드 조회",
            description = "무신사 매거진에서 인기 있는 패션 콘텐츠의 키워드를 가져옵니다. (최대 4개)"
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/fashion")
    public List<KeywordResponseDto> magazine() {
        return magazineService.popularAll();
    }

    @Operation(
            summary = "무신사 랭킹 상품 URL 조회",
            description = "무신사 랭킹에서 상위 N개 상품의 URL을 가져옵니다. (기본값: 5개)"
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/fashion/trending")
    public List<MusinsaRankingLinkDto> trendingKeywords(
            @Parameter(description = "가져올 상품 개수 (기본값: 5)", example = "5")
            @RequestParam(required = false) Integer limit
    ) {
        return fashionCrawlerService.fetchTrendingLinks(limit);
    }

    @Operation(
            summary = "패션 상품 크롤링 실행 (전체 플로우)",
            description = """
                    **전체 크롤링 프로세스를 실행합니다:**
                    1. 무신사 랭킹에서 상위 N개 상품 URL 수집
                    2. 각 상품 페이지에서 카테고리 추출 (예: 숏패딩, 어그부츠)
                    3. 추출한 카테고리로 싸다구 검색
                    4. 싸다구 첫 번째 상품 정보 수집 (이름, 가격, 별점, 이미지)
                    5. Product 테이블에 저장 (중복 체크)

                    **응답:** 저장된 상품 리스트
                    """
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/fashion/crawl")
    public List<ProductResponseDto> crawlProducts(
            @Parameter(description = "크롤링할 상품 개수 (기본값: 5)", example = "3")
            @RequestParam(required = false) Integer limit
    ) {
        List<Product> products = fashionCrawlerService.crawlAndSaveProducts(limit);
        return products.stream()
                .map(ProductResponseDto::from)
                .toList();
    }

    @Operation(
            summary = "저장된 패션 상품 조회",
            description = """
                    DB에 저장된 패션 상품 목록을 최근 순으로 조회합니다.

                    **조회 개수:** 최대 10개
                    **정렬:** 생성일시 내림차순 (최신순)
                    **포함 정보:**
                    - 무신사 원본 URL
                    - 싸다구 상품명, URL, 가격
                    - 별점, 이미지 URL
                    - 추출된 카테고리
                    """
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/fashion/products")
    public List<ProductResponseDto> getProducts() {
        List<Product> products = productRepository.findTop10ByOrderByCreatedAtDesc();
        return products.stream()
                .map(ProductResponseDto::from)
                .toList();
    }
}
