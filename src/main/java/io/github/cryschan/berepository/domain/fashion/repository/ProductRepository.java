package io.github.cryschan.berepository.domain.fashion.repository;

import io.github.cryschan.berepository.domain.fashion.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 카테고리로 상품 검색
     */
    List<Product> findByCategory(String category);

    /**
     * 무신사 상품 URL로 검색 (중복 체크용)
     */
    Optional<Product> findByMusinsaProductUrl(String musinsaProductUrl);

    /**
     * 최근 생성된 상품 조회
     */
    List<Product> findTop10ByOrderByCreatedAtDesc();
}
