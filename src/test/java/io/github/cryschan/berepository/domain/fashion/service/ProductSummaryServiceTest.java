package io.github.cryschan.berepository.domain.fashion.service;

import io.github.cryschan.berepository.domain.ai.exception.AiException;
import io.github.cryschan.berepository.domain.ai.service.SsadaguSummaryService;
import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSummaryService 테스트")
class ProductSummaryServiceTest {

    private ChatClient chatClient;
    private SsadaguSummaryService productSummaryService;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        productSummaryService = new SsadaguSummaryService(chatClient);
    }

    @Nested
    @DisplayName("summary 메서드")
    class Summary {

        @Test
        @DisplayName("정상적인 상품 정보 입력 시 AI 요약 문자열을 반환한다")
        void testSummary_정상_케이스() {
            // Given
            SsadaguProductDto product = SsadaguProductDto.builder()
                    .productName("나이키 에어맥스 운동화")
                    .productUrl("https://example.com/product/123")
                    .price(129000)
                    .rating(4.5)
                    .reviewCount(1234)
                    .imageUrl("https://example.com/images/shoe.jpg")
                    .category("운동화")
                    .productAttributes(Map.of(
                            "브랜드", "나이키",
                            "색상", "화이트",
                            "사이즈", "270mm"
                    ))
                    .build();

            String expectedSummary = "나이키 에어맥스 운동화는 129,000원에 판매되는 인기 상품입니다. " +
                    "평점 4.5점, 리뷰 1,234개로 고객 만족도가 높습니다.";

            when(chatClient.prompt().user(anyString()).call().content()).thenReturn(expectedSummary);

            // When
            String result = productSummaryService.summary(product, 200);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isNotBlank();
            assertThat(result).isEqualTo(expectedSummary);
        }

        @Test
        @DisplayName("null 상품 입력 시 AiException을 던진다")
        void testSummary_Null_입력() {
            // Given
            SsadaguProductDto nullProduct = null;

            // When & Then
            assertThatThrownBy(() -> productSummaryService.summary(nullProduct, 200))
                    .isInstanceOf(AiException.class)
                    .hasMessageContaining("상품 정보");
        }

        @Test
        @DisplayName("필수 필드가 비어있는 상품 입력 시 적절한 기본 메시지를 반환한다")
        void testSummary_빈_데이터() {
            // Given
            SsadaguProductDto emptyProduct = SsadaguProductDto.builder()
                    .productName("")
                    .productUrl("")
                    .price(null)
                    .rating(null)
                    .reviewCount(null)
                    .imageUrl(null)
                    .category(null)
                    .productAttributes(null)
                    .build();

            String fallbackMessage = "상품 정보가 충분하지 않아 요약을 생성할 수 없습니다.";
            when(chatClient.prompt().user(anyString()).call().content()).thenReturn(fallbackMessage);

            // When
            String result = productSummaryService.summary(emptyProduct, 200);

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("productAttributes가 null인 경우에도 정상적으로 요약을 생성한다")
        void testSummary_Null_Attributes() {
            // Given
            SsadaguProductDto productWithNullAttributes = SsadaguProductDto.builder()
                    .productName("테스트 상품")
                    .productUrl("https://example.com/test")
                    .price(50000)
                    .rating(4.0)
                    .reviewCount(100)
                    .imageUrl("https://example.com/image.jpg")
                    .category("의류")
                    .productAttributes(null)
                    .build();

            String expectedSummary = "테스트 상품은 50,000원에 판매되며, 평점 4.0점입니다.";
            when(chatClient.prompt().user(anyString()).call().content()).thenReturn(expectedSummary);

            // When
            String result = productSummaryService.summary(productWithNullAttributes, 200);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isNotBlank();
        }
    }
}
