package io.github.cryschan.berepository.domain.fashion.crawler;

import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("실제 네트워크 크롤링 테스트 - 수동 실행 전용")
@SpringBootTest
@DisplayName("SsadaguCrawler 통합 테스트 (네트워크 의존)")
class SsadaguCrawlerTest {

    @Autowired
    private SsadaguCrawler ssadaguCrawler;

    @Test
    @DisplayName("구두 카테고리 실제 크롤링 테스트")
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
    @DisplayName("패딩 카테고리 실제 크롤링 테스트")
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
    @DisplayName("코트 카테고리 실제 크롤링 테스트")
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
