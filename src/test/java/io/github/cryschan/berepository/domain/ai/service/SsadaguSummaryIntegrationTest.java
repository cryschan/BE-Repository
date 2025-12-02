package io.github.cryschan.berepository.domain.ai.service;

import io.github.cryschan.berepository.domain.ai.dto.response.SsadaguSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Musinsa 크롤링 → Ssadagu 검색 → AI 요약 전체 플로우 통합 테스트
 *
 * 주의: 실제 외부 API 호출이 발생하므로 AI 비용이 발생합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Ssadagu AI 요약 통합 테스트")
class SsadaguSummaryIntegrationTest {

    @Autowired
    private SsadaguIntegrationService ssadaguIntegrationService;

    @Nested
    @DisplayName("단일 카테고리 검색 + AI 요약")
    class SearchAndSummarizeTest {

        @Test
        @DisplayName("숏패딩 카테고리로 검색 + AI 요약")
        void searchAndSummarize_ShortPadding_Success() {
            // Given
            String category = "숏패딩";
            int charLimit = 200;

            System.out.println("=== 카테고리: " + category + " ===");

            // When: 통합 메서드 호출 (검색 + 요약 한 번에)
            SsadaguSummaryResponse response = ssadaguIntegrationService.searchAndSummarize(category, charLimit);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.product()).isNotNull();
            assertThat(response.summary()).isNotBlank();

            System.out.println("상품명: " + response.product().productName());
            System.out.println("가격: " + response.product().price() + "원");
            System.out.println("URL: " + response.product().productUrl());
            System.out.println("\nAI 생성 홍보 문구:");
            System.out.println(response.summary());
            System.out.println("글자수: " + response.summary().length());
        }

        @Test
        @DisplayName("운동화 카테고리로 검색 + AI 요약")
        void searchAndSummarize_Sneakers_Success() {
            // Given
            String category = "운동화";
            int charLimit = 200;

            System.out.println("=== 카테고리: " + category + " ===");

            // When
            SsadaguSummaryResponse response = ssadaguIntegrationService.searchAndSummarize(category, charLimit);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.product()).isNotNull();
            assertThat(response.summary()).isNotBlank();

            System.out.println("상품명: " + response.product().productName());
            System.out.println("가격: " + response.product().price() + "원");
            System.out.println("\nAI 생성 홍보 문구:");
            System.out.println(response.summary());
            System.out.println("글자수: " + response.summary().length());
        }

        @Test
        @DisplayName("긴 홍보 문구 (1500자) 생성")
        void searchAndSummarize_LongCharLimit_Success() {
            // Given
            String category = "맨투맨";
            int charLimit = 1500;

            System.out.println("=== 카테고리: " + category + " (1500자) ===");

            // When
            SsadaguSummaryResponse response = ssadaguIntegrationService.searchAndSummarize(category, charLimit);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.summary()).isNotBlank();

            System.out.println("상품명: " + response.product().productName());
            System.out.println("\nAI 생성 홍보 문구 (1500자):");
            System.out.println(response.summary());
            System.out.println("글자수: " + response.summary().length());
        }
    }

    @Nested
    @DisplayName("무신사 트렌딩 기반 크롤링 + AI 요약")
    class CrawlAndSummarizeFromTrendingTest {

        @Test
        @DisplayName("무신사 상위 1개 상품 → 카테고리 추출 → Ssadagu 검색 → AI 요약")
        void crawlAndSummarize_Top1_Success() {
            // Given
            int limit = 1;
            int charLimit = 200;

            System.out.println("=== 무신사 트렌딩 상위 " + limit + "개 크롤링 ===");

            // When: 전체 플로우 한 번에 호출
            List<SsadaguSummaryResponse> responses = ssadaguIntegrationService.crawlAndSummarizeFromTrending(limit, charLimit);

            // Then
            assertThat(responses).isNotEmpty();

            for (int i = 0; i < responses.size(); i++) {
                SsadaguSummaryResponse response = responses.get(i);
                System.out.println("\n--- 상품 " + (i + 1) + " ---");
                System.out.println("상품명: " + response.product().productName());
                System.out.println("가격: " + response.product().price() + "원");
                System.out.println("카테고리: " + response.product().category());
                System.out.println("\nAI 홍보 문구:");
                System.out.println(response.summary());
            }
        }

        @Test
        @DisplayName("무신사 상위 3개 상품 → 각각 AI 요약 생성")
        void crawlAndSummarize_Top3_Success() {
            // Given
            int limit = 3;
            int charLimit = 200;

            System.out.println("=== 무신사 트렌딩 상위 " + limit + "개 크롤링 ===");

            // When
            List<SsadaguSummaryResponse> responses = ssadaguIntegrationService.crawlAndSummarizeFromTrending(limit, charLimit);

            // Then
            assertThat(responses).hasSizeGreaterThanOrEqualTo(1);

            System.out.println("총 " + responses.size() + "개 상품 요약 생성 완료\n");

            for (int i = 0; i < responses.size(); i++) {
                SsadaguSummaryResponse response = responses.get(i);
                System.out.println("========================================");
                System.out.println("상품 " + (i + 1) + " / " + responses.size());
                System.out.println("========================================");
                System.out.println("상품명: " + response.product().productName());
                System.out.println("가격: " + response.product().price() + "원");
                System.out.println("카테고리: " + response.product().category());
                System.out.println("URL: " + response.product().productUrl());
                System.out.println("\nAI 홍보 문구:");
                System.out.println(response.summary());
                System.out.println("글자수: " + response.summary().length());
                System.out.println();
            }
        }
    }
}
