package io.github.cryschan.berepository.domain.fashion.dto.response;

import io.github.cryschan.berepository.domain.fashion.entity.Product;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Product 엔티티를 클라이언트에 반환하는 DTO
 */
@Builder
public record ProductResponseDto(
        Long id,
        String category,
        String musinsaProductUrl,
        String ssadaguProductName,
        String ssadaguProductUrl,
        Integer price,
        Double rating,
        Integer reviewCount,
        String imageUrl,
        LocalDateTime createdAt
) {
    public static ProductResponseDto from(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .category(product.getCategory())
                .musinsaProductUrl(product.getMusinsaProductUrl())
                .ssadaguProductName(product.getSsadaguProductName())
                .ssadaguProductUrl(product.getSsadaguProductUrl())
                .price(product.getPrice())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
