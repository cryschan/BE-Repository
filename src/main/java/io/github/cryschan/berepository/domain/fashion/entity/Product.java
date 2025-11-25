package io.github.cryschan.berepository.domain.fashion.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 크롤링한 상품 정보를 저장하는 엔티티
 */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    @Column(name = "musinsa_product_url", nullable = false)
    private String musinsaProductUrl;

    @Column(name = "ssadagu_product_name")
    private String ssadaguProductName;

    @Column(name = "ssadagu_product_url")
    private String ssadaguProductUrl;

    @Column
    private Integer price;

    @Column
    private Double rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Product(
            String category,
            String musinsaProductUrl,
            String ssadaguProductName,
            String ssadaguProductUrl,
            Integer price,
            Double rating,
            Integer reviewCount,
            String imageUrl
    ) {
        this.category = category;
        this.musinsaProductUrl = musinsaProductUrl;
        this.ssadaguProductName = ssadaguProductName;
        this.ssadaguProductUrl = ssadaguProductUrl;
        this.price = price;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.imageUrl = imageUrl;
    }
}
