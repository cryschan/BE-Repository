package io.github.cryschan.berepository.domain.fashion.dto.response;

import lombok.Builder;

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
        String category
) {
}
