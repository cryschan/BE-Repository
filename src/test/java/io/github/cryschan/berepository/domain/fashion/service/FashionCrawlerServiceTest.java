package io.github.cryschan.berepository.domain.fashion.service;

import io.github.cryschan.berepository.domain.fashion.crawler.SsadaguCrawler;
import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import io.github.cryschan.berepository.domain.fashion.exception.FashionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FashionCrawlerService 테스트")
class FashionCrawlerServiceTest {

    @Mock
    private SsadaguCrawler ssadaguCrawler;

    @InjectMocks
    private FashionCrawlerService fashionCrawlerService;

    @Test
    @DisplayName("카테고리로 상품 검색 성공")
    void searchProductByCategory_Success() {
        // given
        String category = "패딩";
        SsadaguProductDto expectedProduct = createMockProduct(category);
        given(ssadaguCrawler.searchFirstProduct(category)).willReturn(expectedProduct);

        // when
        SsadaguProductDto result = fashionCrawlerService.searchProductByCategory(category);

        // then
        assertThat(result).isNotNull();
        assertThat(result.productName()).isEqualTo("테스트 패딩");
        assertThat(result.category()).isEqualTo(category);
        verify(ssadaguCrawler).searchFirstProduct(category);
    }

    @Test
    @DisplayName("카테고리가 null이면 예외 발생")
    void searchProductByCategory_NullCategory() {
        // when & then
        assertThatThrownBy(() -> fashionCrawlerService.searchProductByCategory(null))
                .isInstanceOf(FashionException.class)
                .hasMessageContaining("카테고리는 필수입니다");
    }

    @Test
    @DisplayName("카테고리가 빈 문자열이면 예외 발생")
    void searchProductByCategory_EmptyCategory() {
        // when & then
        assertThatThrownBy(() -> fashionCrawlerService.searchProductByCategory(""))
                .isInstanceOf(FashionException.class)
                .hasMessageContaining("카테고리는 필수입니다");
    }

    @Test
    @DisplayName("상품을 찾지 못하면 예외 발생")
    void searchProductByCategory_NoProductFound() {
        // given
        String category = "존재하지않는카테고리";
        given(ssadaguCrawler.searchFirstProduct(category)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> fashionCrawlerService.searchProductByCategory(category))
                .isInstanceOf(FashionException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("여러 카테고리로 상품 크롤링 성공")
    void crawlProductsByCategories_Success() {
        // given
        List<String> categories = List.of("패딩", "구두", "코트");
        given(ssadaguCrawler.searchFirstProduct("패딩")).willReturn(createMockProduct("패딩"));
        given(ssadaguCrawler.searchFirstProduct("구두")).willReturn(createMockProduct("구두"));
        given(ssadaguCrawler.searchFirstProduct("코트")).willReturn(createMockProduct("코트"));

        // when
        List<SsadaguProductDto> results = fashionCrawlerService.crawlProductsByCategories(categories);

        // then
        assertThat(results).hasSize(3);
        assertThat(results.get(0).category()).isEqualTo("패딩");
        assertThat(results.get(1).category()).isEqualTo("구두");
        assertThat(results.get(2).category()).isEqualTo("코트");
        verify(ssadaguCrawler, times(3)).searchFirstProduct(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("카테고리 목록이 null이면 빈 리스트 반환")
    void crawlProductsByCategories_NullCategories() {
        // when
        List<SsadaguProductDto> results = fashionCrawlerService.crawlProductsByCategories(null);

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("카테고리 목록이 비어있으면 빈 리스트 반환")
    void crawlProductsByCategories_EmptyCategories() {
        // when
        List<SsadaguProductDto> results = fashionCrawlerService.crawlProductsByCategories(List.of());

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("일부 카테고리에서 상품을 찾지 못해도 다른 카테고리는 계속 처리")
    void crawlProductsByCategories_PartialSuccess() {
        // given
        List<String> categories = List.of("패딩", "존재하지않음", "구두");
        given(ssadaguCrawler.searchFirstProduct("패딩")).willReturn(createMockProduct("패딩"));
        given(ssadaguCrawler.searchFirstProduct("존재하지않음")).willReturn(null);
        given(ssadaguCrawler.searchFirstProduct("구두")).willReturn(createMockProduct("구두"));

        // when
        List<SsadaguProductDto> results = fashionCrawlerService.crawlProductsByCategories(categories);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).category()).isEqualTo("패딩");
        assertThat(results.get(1).category()).isEqualTo("구두");
    }

    @Test
    @DisplayName("카테고리 크롤링 중 예외 발생해도 다른 카테고리는 계속 처리")
    void crawlProductsByCategories_ExceptionHandling() {
        // given
        List<String> categories = List.of("패딩", "오류발생", "구두");
        given(ssadaguCrawler.searchFirstProduct("패딩")).willReturn(createMockProduct("패딩"));
        given(ssadaguCrawler.searchFirstProduct("오류발생"))
                .willThrow(new RuntimeException("크롤링 오류"));
        given(ssadaguCrawler.searchFirstProduct("구두")).willReturn(createMockProduct("구두"));

        // when
        List<SsadaguProductDto> results = fashionCrawlerService.crawlProductsByCategories(categories);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).category()).isEqualTo("패딩");
        assertThat(results.get(1).category()).isEqualTo("구두");
    }

    private SsadaguProductDto createMockProduct(String category) {
        return SsadaguProductDto.builder()
                .productName("테스트 " + category)
                .productUrl("https://ssadagu.kr/test")
                .price(29900)
                .rating(4.5)
                .reviewCount(100)
                .imageUrl("https://example.com/image.jpg")
                .category(category)
                .productAttributes(Map.of("소재", "폴리에스터"))
                .build();
    }
}
