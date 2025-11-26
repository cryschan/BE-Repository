package io.github.cryschan.berepository.domain.fashion.controller;

import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import io.github.cryschan.berepository.domain.fashion.service.FashionCrawlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Fashion", description = "패션 상품 크롤링 API (테스트용, 로그인 필수)")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
@RequestMapping("/api/keyword")
@RestController
public class FashionController {

    private final FashionCrawlerService fashionCrawlerService;

    @Operation(
            summary = "패션 상품 크롤링 테스트 (전체 플로우)",
            description = """
                    **전체 크롤링 프로세스를 테스트합니다:**
                    1. 무신사 랭킹에서 상위 N개 상품 URL 수집
                    2. 각 상품 페이지에서 카테고리 추출 (예: 숏패딩, 어그부츠)
                    3. 추출한 카테고리로 싸다구 검색
                    4. 싸다구 첫 번째 상품 정보 수집 (이름, 가격, 별점, 이미지)

                    **주의:** DB에 저장하지 않고 즉시 반환만 합니다. (테스트용)
                    """
    )
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/fashion/crawl")
    public List<SsadaguProductDto> crawlProducts(
            @Parameter(description = "크롤링할 상품 개수 (기본값: 5)", example = "3")
            @RequestParam(required = false) Integer limit
    ) {
        return fashionCrawlerService.crawlProductsFromTrending(limit);
    }

    @Operation(
            summary = "카테고리별 상품 검색 테스트",
            description = """
                    특정 카테고리로 싸다구에서 상품을 검색합니다.

                    **예시:** category=패딩, category=구두
                    """
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/fashion/search")
    public SsadaguProductDto searchByCategory(
            @Parameter(description = "검색할 카테고리", example = "패딩", required = true)
            @RequestParam String category
    ) {
        return fashionCrawlerService.searchProductByCategory(category);
    }
}
