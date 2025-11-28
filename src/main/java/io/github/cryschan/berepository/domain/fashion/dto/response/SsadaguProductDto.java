package io.github.cryschan.berepository.domain.fashion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Map;

@Schema(description = "싸다구 상품 정보")
@Builder
public record SsadaguProductDto(
        @Schema(description = "상품명", example = "나이키 에어맥스 운동화")
        String productName,

        @Schema(description = "상품 URL", example = "https://ssadagu.com/product/123")
        String productUrl,

        @Schema(description = "가격 (원)", example = "129000")
        Integer price,

        @Schema(description = "평점 (5점 만점)", example = "4.5")
        Double rating,

        @Schema(description = "리뷰 수", example = "1234")
        Integer reviewCount,

        @Schema(description = "상품 이미지 URL", example = "https://ssadagu.com/images/shoe.jpg")
        String imageUrl,

        @Schema(description = "카테고리", example = "운동화")
        String category,

        @Schema(description = "상품 추가 속성", example = "{\"브랜드\": \"나이키\", \"색상\": \"화이트\", \"사이즈\": \"270mm\"}")
        Map<String, String> productAttributes
) {
}
