package io.github.cryschan.berepository.domain.ai.dto.response;

import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "싸다구 상품 AI 요약 응답")
public record SsadaguSummaryResponse(
        @Schema(description = "싸다구 상품 정보")
        SsadaguProductDto product,

        @Schema(description = "AI가 생성한 상품 요약", example = "나이키 에어맥스 운동화는 129,000원에 판매되는 인기 상품입니다. 평점 4.5점, 리뷰 1,234개로 고객 만족도가 높으며 가격 대비 품질이 우수합니다.")
        String summary
) {
    public static SsadaguSummaryResponse from(SsadaguProductDto product, String summary) {
        return new SsadaguSummaryResponse(product, summary);
    }
}
