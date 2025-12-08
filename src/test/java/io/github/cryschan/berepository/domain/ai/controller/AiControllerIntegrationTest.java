package io.github.cryschan.berepository.domain.ai.controller;

import io.github.cryschan.berepository.domain.ai.dto.response.SsadaguSummaryResponse;
import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("DB 연결 문제로 통합 테스트 비활성화 - 로컬 PostgreSQL 필요")
@Deprecated
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("AiController 통합 테스트")
class AiControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Nested
    @DisplayName("싸다구 상품 AI 홍보 API")
    class SsadaguSummaryTest {

        @Test
        @DisplayName("201 성공: 상품 정보로 AI 홍보 문구 생성")
        void ssadaguSummary_Success() {
            // Given
            SsadaguProductDto product = SsadaguProductDto.builder()
                    .productName("나이키 에어맥스 운동화")
                    .productUrl("https://ssadagu.com/product/123")
                    .price(129000)
                    .rating(4.5)
                    .reviewCount(1234)
                    .imageUrl("https://ssadagu.com/images/shoe.jpg")
                    .category("운동화")
                    .productAttributes(Map.of(
                            "브랜드", "나이키",
                            "색상", "화이트",
                            "사이즈", "270mm"
                    ))
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SsadaguProductDto> request = new HttpEntity<>(product, headers);

            // When
            ResponseEntity<SsadaguSummaryResponse> response = restTemplate.postForEntity(
                    "/api/ai/ssadagu/summary?charLimit=200",
                    request,
                    SsadaguSummaryResponse.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().product()).isNotNull();
            assertThat(response.getBody().product().productName()).isEqualTo("나이키 에어맥스 운동화");
            assertThat(response.getBody().summary()).isNotBlank();

            // 로그 출력 (테스트 결과 확인용)
            System.out.println("=== AI 생성 홍보 문구 ===");
            System.out.println(response.getBody().summary());
            System.out.println("글자수: " + response.getBody().summary().length());
        }

        @Test
        @DisplayName("201 성공: 글자수 제한 1500자로 AI 홍보 문구 생성")
        void ssadaguSummary_LongCharLimit_Success() {
            // Given
            SsadaguProductDto product = SsadaguProductDto.builder()
                    .productName("삼성 갤럭시 버즈3")
                    .productUrl("https://ssadagu.com/product/456")
                    .price(179000)
                    .rating(4.8)
                    .reviewCount(892)
                    .imageUrl("https://ssadagu.com/images/buds.jpg")
                    .category("이어폰")
                    .productAttributes(Map.of(
                            "브랜드", "삼성",
                            "색상", "블랙",
                            "노이즈캔슬링", "지원"
                    ))
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SsadaguProductDto> request = new HttpEntity<>(product, headers);

            // When
            ResponseEntity<SsadaguSummaryResponse> response = restTemplate.postForEntity(
                    "/api/ai/ssadagu/summary?charLimit=1500",
                    request,
                    SsadaguSummaryResponse.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().summary()).isNotBlank();

            // 로그 출력
            System.out.println("=== AI 생성 홍보 문구 (1500자) ===");
            System.out.println(response.getBody().summary());
            System.out.println("글자수: " + response.getBody().summary().length());
        }

        @Test
        @DisplayName("201 성공: 최소 정보만으로 AI 홍보 문구 생성")
        void ssadaguSummary_MinimalInfo_Success() {
            // Given
            SsadaguProductDto product = SsadaguProductDto.builder()
                    .productName("테스트 상품")
                    .price(50000)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SsadaguProductDto> request = new HttpEntity<>(product, headers);

            // When
            ResponseEntity<SsadaguSummaryResponse> response = restTemplate.postForEntity(
                    "/api/ai/ssadagu/summary?charLimit=200",
                    request,
                    SsadaguSummaryResponse.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().summary()).isNotBlank();

            System.out.println("=== AI 생성 홍보 문구 (최소 정보) ===");
            System.out.println(response.getBody().summary());
        }

        @Test
        @DisplayName("400 실패: null 상품 정보로 요청 시 에러")
        void ssadaguSummary_NullProduct_Fail() {
            // Given
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>("null", headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/ai/ssadagu/summary?charLimit=200",
                    request,
                    String.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}
