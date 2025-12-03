package io.github.cryschan.berepository.domain.fashion.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 무신사 상품 페이지에서 __NEXT_DATA__ JSON을 파싱하여 카테고리를 추출하는 크롤러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MusinsaProductDetailCrawler {

    private static final int TIMEOUT_MS = 10000;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final Pattern NEXT_DATA_PATTERN = Pattern.compile(
            "<script id=\"__NEXT_DATA__\" type=\"application/json\">([^<]+)</script>"
    );

    private final ObjectMapper objectMapper;

    /**
     * 무신사 상품 URL에서 카테고리를 추출
     *
     * @param productUrl 무신사 상품 URL (예: https://www.musinsa.com/products/1234567)
     * @return 추출된 카테고리 (예: "겨울 싱글 코트", "숏패딩", "어그부츠")
     */
    public String extractCategory(String productUrl) {
        if (productUrl == null || productUrl.isBlank()) {
            log.warn("Product URL is null or empty");
            return null;
        }

        try {
            log.debug("Fetching product page from: {}", productUrl);

            // 1. HTML 페이지 가져오기
            Document doc = Jsoup.connect(productUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .get();

            // 2. __NEXT_DATA__ JSON 추출
            String html = doc.html();
            Matcher matcher = NEXT_DATA_PATTERN.matcher(html);

            if (!matcher.find()) {
                log.warn("Failed to find __NEXT_DATA__ in page: {}", productUrl);
                return null;
            }

            String jsonData = matcher.group(1);
            log.debug("Extracted __NEXT_DATA__ JSON (length: {})", jsonData.length());

            // 3. JSON 파싱
            JsonNode root = objectMapper.readTree(jsonData);
            JsonNode pagePropsData = root.path("props").path("pageProps").path("meta").path("data");

            if (pagePropsData.isMissingNode()) {
                log.warn("Failed to find props.pageProps.meta.data in __NEXT_DATA__");
                return null;
            }

            // 4. 가장 구체적인 카테고리 추출 (depth3 > depth2 > depth1 순서로 시도)
            JsonNode categoryNode = pagePropsData.path("category");

            // depth3 시도 (가장 구체적)
            String category = categoryNode.path("categoryDepth3Name").asText(null);
            if (category != null && !category.isBlank()) {
                log.info("Extracted category (depth3): {} from URL: {}", category, productUrl);
                return category;
            }

            // depth2 시도 (중분류)
            category = categoryNode.path("categoryDepth2Name").asText(null);
            if (category != null && !category.isBlank()) {
                log.info("Extracted category (depth2): {} from URL: {}", category, productUrl);
                return category;
            }

            // depth1 시도 (대분류)
            category = categoryNode.path("categoryDepth1Name").asText(null);
            if (category != null && !category.isBlank()) {
                log.info("Extracted category (depth1): {} from URL: {}", category, productUrl);
                return category;
            }

            log.warn("Failed to extract category from __NEXT_DATA__: {}", productUrl);
            return null;

        } catch (IOException e) {
            log.error("Failed to fetch product page from URL: {}", productUrl, e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error while extracting category from URL: {}", productUrl, e);
            return null;
        }
    }

}
