package io.github.cryschan.berepository.domain.ai.controller;

import io.github.cryschan.berepository.domain.ai.dto.response.SsadaguSummaryResponse;
import io.github.cryschan.berepository.domain.ai.service.AiService;
import io.github.cryschan.berepository.domain.ai.service.SsadaguSummaryService;
import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI", description = "AI 홍보 API")
@RequiredArgsConstructor
@RestController
public class AiController {

    private final AiService aiService;
    private final SsadaguSummaryService ssadaguSummaryService;

    @Operation(summary = "AI 댓글 생성", description = "입력된 메시지에 대한 AI 응답을 생성합니다")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "AI 댓글 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "\"AI가 생성한 응답 메시지입니다.\""
                            )
                    )
            )
    })
    @PostMapping("/api/ai/comment")
    public String aiComment(@RequestBody String comment) {
        return aiService.comment(comment);
    }

    @Operation(summary = "싸다구 상품 AI 홍보", description = "싸다구 상품 정보를 AI가 자연스러운 한국어로 홍보합니다")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "AI 홍보 문구 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SsadaguSummaryResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "product": {
                                                    "productName": "나이키 에어맥스 운동화",
                                                    "productUrl": "https://ssadagu.com/product/123",
                                                    "price": 129000,
                                                    "rating": 4.5,
                                                    "reviewCount": 1234,
                                                    "imageUrl": "https://ssadagu.com/images/shoe.jpg",
                                                    "category": "운동화",
                                                    "productAttributes": {
                                                        "브랜드": "나이키",
                                                        "색상": "화이트"
                                                    }
                                                },
                                                "summary": "나이키 에어맥스 운동화는 129,000원에 판매되는 인기 상품입니다. 평점 4.5점, 리뷰 1,234개로 고객 만족도가 높습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 상품 정보",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "상품 정보가 null입니다",
                                                "status": 400,
                                                "code": "AI001",
                                                "timestamp": "2024-11-27T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            )
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/ai/ssadagu/summary")
    public SsadaguSummaryResponse ssadaguSummary(
            @RequestBody SsadaguProductDto product,
            @Parameter(description = "AI 홍보 최대 글자수", example = "1500")
            @RequestParam(defaultValue = "200") int charLimit
    ) {
        String summary = ssadaguSummaryService.summary(product, charLimit);
        return SsadaguSummaryResponse.from(product, summary);
    }
}
