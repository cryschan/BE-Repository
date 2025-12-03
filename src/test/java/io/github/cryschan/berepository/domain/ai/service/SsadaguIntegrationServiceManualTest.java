package io.github.cryschan.berepository.domain.ai.service;

import io.github.cryschan.berepository.domain.ai.dto.response.SsadaguSummaryResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 무신사 → 싸다구 플로우 수동 테스트
 * 실제 네트워크 요청을 수행
 */
@SpringBootTest
@Disabled("실제 네트워크 크롤링 테스트 - 수동 실행 전용")
class SsadaguIntegrationServiceManualTest {

    @Autowired
    private SsadaguIntegrationService ssadaguIntegrationService;

    @Test
    void testSearchByCategoryName_신발() {
        // given
        String categoryName = "신발";
        int charLimit = 200;

        // when
        System.out.println("\n========== 신발 카테고리 테스트 시작 ==========");
        SsadaguSummaryResponse response = ssadaguIntegrationService.searchAndSummarizeByCategoryName(categoryName, charLimit);

        // then
        System.out.println("\n========== 결과 ==========");
        if (response != null) {
            System.out.println("✅ 상품 검색 성공");
            System.out.println("상품명: " + response.product().productName());
            System.out.println("가격: " + response.product().price());
            System.out.println("카테고리: " + response.product().category());
            System.out.println("제목: " + response.title());
            System.out.println("요약: " + response.summary());

            assertThat(response.product().productName()).isNotBlank();
            // 무신사에서 추출한 카테고리 확인 (예: "라이프스타일화")
            System.out.println("추출된 카테고리: " + response.product().category());
        } else {
            System.out.println("❌ 상품 검색 실패");
        }
    }

    @Test
    void testSearchByCategoryName_상의() {
        // given
        String categoryName = "상의";
        int charLimit = 200;

        // when
        System.out.println("\n========== 상의 카테고리 테스트 시작 ==========");
        SsadaguSummaryResponse response = ssadaguIntegrationService.searchAndSummarizeByCategoryName(categoryName, charLimit);

        // then
        System.out.println("\n========== 결과 ==========");
        if (response != null) {
            System.out.println("✅ 상품 검색 성공");
            System.out.println("상품명: " + response.product().productName());
            System.out.println("가격: " + response.product().price());
            System.out.println("카테고리: " + response.product().category());
            System.out.println("제목: " + response.title());

            assertThat(response.product().productName()).isNotBlank();
        } else {
            System.out.println("❌ 상품 검색 실패");
        }
    }

    @Test
    void testSearchByCategoryName_아우터() {
        // given
        String categoryName = "아우터";
        int charLimit = 200;

        // when
        System.out.println("\n========== 아우터 카테고리 테스트 시작 ==========");
        SsadaguSummaryResponse response = ssadaguIntegrationService.searchAndSummarizeByCategoryName(categoryName, charLimit);

        // then
        System.out.println("\n========== 결과 ==========");
        if (response != null) {
            System.out.println("✅ 상품 검색 성공");
            System.out.println("상품명: " + response.product().productName());
            System.out.println("가격: " + response.product().price());
            System.out.println("카테고리: " + response.product().category());

            assertThat(response.product().productName()).isNotBlank();
        } else {
            System.out.println("❌ 상품 검색 실패");
        }
    }
}
