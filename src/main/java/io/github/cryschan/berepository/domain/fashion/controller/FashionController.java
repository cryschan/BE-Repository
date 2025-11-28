package io.github.cryschan.berepository.domain.fashion.controller;

import io.github.cryschan.berepository.domain.blogtemplate.dto.BlogContentGenerationRequest;
import io.github.cryschan.berepository.domain.fashion.service.FashionCrawlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "크롤링", description = "패션 상품 크롤링 API")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
@RequestMapping("/api/crawling")
@RestController
public class FashionController {

    private final FashionCrawlerService fashionCrawlerService;

    @Operation(
            summary = "사용자 템플릿 기반 상품 크롤링",
            description = """
                    **로그인한 사용자의 블로그 템플릿을 기반으로 상품을 크롤링합니다.**

                    ### 동작 방식:
                    1. 사용자의 블로그 템플릿 조회
                    2. 템플릿에 설정된 카테고리로 싸다구에서 상품 크롤링
                    3. 템플릿 설정 + 크롤링 결과를 합쳐서 반환

                    ### 반환 데이터:
                    - 사용자 정보 (userId, templateTitle)
                    - 템플릿 설정 (charLimit, includeImages, imageCount, platforms)
                    - 크롤링된 상품 정보 (crawledProducts)

                    **이 데이터는 AI 블로그 글 생성에 사용됩니다.**
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "크롤링 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BlogContentGenerationRequest.class),
                            examples = @ExampleObject(
                                    name = "크롤링 성공 예시",
                                    value = """
                                            {
                                              "userId": 1,
                                              "templateTitle": "패션 아이템 추천",
                                              "charLimit": 1000,
                                              "includeImages": true,
                                              "imageCount": 3,
                                              "platforms": ["티스토리", "네이버"],
                                              "crawledProducts": [
                                                {
                                                  "productName": "남성 숏패딩",
                                                  "productUrl": "https://ssadagu.kr/shop/view.php?idx=12345",
                                                  "price": 29900,
                                                  "rating": 4.5,
                                                  "reviewCount": null,
                                                  "imageUrl": "https://example.com/image.jpg",
                                                  "category": "패딩",
                                                  "productAttributes": {
                                                    "인기 요소": "캐주얼",
                                                    "소재": "폴리에스터"
                                                  }
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "리소스를 찾을 수 없음 (블로그 템플릿 또는 상품)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "블로그 템플릿 없음",
                                            value = """
                                                    {
                                                      "errorCode": "BT001",
                                                      "message": "블로그 템플릿을 찾을 수 없습니다. User ID: 1"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "상품 없음",
                                            value = """
                                                    {
                                                      "errorCode": "FS003",
                                                      "message": "'패딩' 카테고리에서 상품을 찾을 수 없습니다"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/fashion")
    public BlogContentGenerationRequest crawlFashionProducts(
            @AuthenticationPrincipal Long userId
    ) {
        return fashionCrawlerService.generateBlogContentRequest(userId);
    }
}
