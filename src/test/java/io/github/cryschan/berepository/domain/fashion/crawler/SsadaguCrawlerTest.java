package io.github.cryschan.berepository.domain.fashion.crawler;

import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SsadaguCrawlerTest {

    @Autowired
    private SsadaguCrawler ssadaguCrawler;

    @Test
    void testSearchFirstProduct_구두() {
        // Given
        String category = "구두";

        // When
        SsadaguProductDto result = ssadaguCrawler.searchFirstProduct(category);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.productName()).isNotBlank();
        assertThat(result.productUrl()).isNotBlank();

        // productAttributes 확인
        System.out.println("=== Product Attributes ===");
        if (result.productAttributes() != null) {
            result.productAttributes().forEach((key, value) ->
                System.out.println(key + " = " + value)
            );
            assertThat(result.productAttributes()).isNotEmpty();
        } else {
            System.out.println("productAttributes is NULL!");
        }
    }

    @Test
    void testSearchFirstProduct_패딩() {
        // Given
        String category = "패딩";

        // When
        SsadaguProductDto result = ssadaguCrawler.searchFirstProduct(category);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.productName()).isNotBlank();
        assertThat(result.productUrl()).contains("view.php");
        assertThat(result.productUrl()).contains("platform=");
        assertThat(result.productUrl()).contains("num_iid=");

        System.out.println("=== 패딩 검색 결과 ===");
        System.out.println("상품명: " + result.productName());
        System.out.println("URL: " + result.productUrl());
        System.out.println("가격: " + result.price());
        System.out.println("평점: " + result.rating());
    }

    @Test
    void testSearchFirstProduct_코트() {
        // Given
        String category = "코트";

        // When
        SsadaguProductDto result = ssadaguCrawler.searchFirstProduct(category);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.productName()).isNotBlank();

        System.out.println("=== 코트 검색 결과 ===");
        System.out.println("상품명: " + result.productName());
        System.out.println("URL: " + result.productUrl());
    }
}
