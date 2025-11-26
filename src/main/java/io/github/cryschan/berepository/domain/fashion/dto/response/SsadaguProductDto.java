package io.github.cryschan.berepository.domain.fashion.dto.response;

import lombok.Builder;

import java.util.Map;

/**
 * 싸다구에서 크롤링한 상품 정보 DTO
 */
@Builder
public record SsadaguProductDto(
        String productName,
        String productUrl,
        Integer price,
        Double rating,
        Integer reviewCount,
        String imageUrl,
        String category,
        Map<String, String> productAttributes  // 상품 정보 (예: 인기 요소, 발가락 모양, 신발 어퍼 소재 등)
) {
}
