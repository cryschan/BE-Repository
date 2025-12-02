package io.github.cryschan.berepository.domain.fashion.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 싸다구(Ssadagu) 사이트에서 상품 정보를 크롤링하는 크롤러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SsadaguCrawler {

    private static final String SSADAGU_BASE_URL = "https://ssadagu.kr";
    private static final String SEARCH_API_URL = SSADAGU_BASE_URL + "/shop/ajax.infinity_shop_list.php";
    private static final String SEARCH_URL_PATTERN = SSADAGU_BASE_URL + "/shop/search.php?ss_tx=%s";
    private static final int TIMEOUT_MS = 10000;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    private final ObjectMapper objectMapper;

    /**
     * 카테고리로 상품을 검색하고 첫 번째 상품의 정보를 반환
     *
     * 싸다구 검색 API(/shop/ajax.infinity_shop_list.php)를 사용하여 실제 검색 기능 구현
     *
     * @param category 검색할 카테고리 (예: "숏패딩", "구두")
     * @return 첫 번째 상품 정보 (검색 결과가 없으면 null)
     */
    public SsadaguProductDto searchFirstProduct(String category) {
        if (category == null || category.isBlank()) {
            log.warn("Category is null or empty");
            return null;
        }

        try {
            // 1단계: 검색 API 호출
            String productUrl = searchProductUrlViaApi(category);

            if (productUrl == null) {
                log.warn("No product found for category: {}", category);
                return null;
            }

            log.info("Found product URL for category '{}': {}", category, productUrl);

            // 2단계: 상품 상세 페이지 방문
            log.debug("Fetching product detail from: {}", productUrl);
            Document productDoc = Jsoup.connect(productUrl)
                    .userAgent(USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .referrer(SSADAGU_BASE_URL)
                    .timeout(TIMEOUT_MS)
                    .get();

            // 3단계: 상품 정보 추출
            return extractProductInfo(productDoc, productUrl, category);

        } catch (IOException e) {
            log.error("Failed to fetch Ssadagu product for category: {}", category, e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error while fetching Ssadagu product for category: {}", category, e);
            return null;
        }
    }

    /**
     * 싸다구 검색 API를 호출하여 첫 번째 상품 URL 반환 (재시도 포함)
     *
     * @param keyword 검색 키워드
     * @return 첫 번째 상품 URL (검색 결과가 없으면 null)
     */
    private String searchProductUrlViaApi(String keyword) {
        int maxRetries = 2;
        int retryDelayMs = 2000; // 2초

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 1) {
                    log.info("Retry attempt {}/{} for keyword: {}", attempt, maxRetries, keyword);
                    Thread.sleep(retryDelayMs);
                }
                return searchProductUrlViaApiInternal(keyword);
            } catch (org.jsoup.HttpStatusException e) {
                int statusCode = e.getStatusCode();
                log.warn("HTTP {} error on attempt {}/{} for keyword: {}", statusCode, attempt, maxRetries, keyword);

                // 502 Bad Gateway 또는 503 Service Unavailable인 경우 재시도
                if ((statusCode == 502 || statusCode == 503) && attempt < maxRetries) {
                    log.info("Retrying after {}ms...", retryDelayMs);
                    continue;
                }

                log.error("Failed to call Ssadagu search API for keyword: {} (HTTP {})", keyword, statusCode, e);
                return null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while waiting for retry", e);
                return null;
            } catch (IOException e) {
                log.error("Failed to call Ssadagu search API for keyword: {} on attempt {}/{}", keyword, attempt, maxRetries, e);
                if (attempt < maxRetries) {
                    log.info("Retrying after {}ms...", retryDelayMs);
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                    continue;
                }
                return null;
            } catch (Exception e) {
                log.error("Unexpected error while searching Ssadagu for keyword: {}", keyword, e);
                return null;
            }
        }
        return null;
    }

    /**
     * 싸다구 검색 API 호출 실제 로직
     */
    private String searchProductUrlViaApiInternal(String keyword) throws IOException {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

            // 1단계: 먼저 검색 페이지를 방문해서 쿠키 받기 (세션 생성)
            String searchPageUrl = String.format(SEARCH_URL_PATTERN, encodedKeyword);
            log.debug("Step 1: Visiting search page to get session cookies: {}", searchPageUrl);

            Map<String, String> cookies = Jsoup.connect(searchPageUrl)
                    .userAgent(USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .timeout(TIMEOUT_MS)
                    .execute()
                    .cookies();

            log.debug("Received {} cookies from search page", cookies.size());

            // 2단계: 받은 쿠키와 함께 API 호출
            String requestBody = String.format(
                "page_div_id=infinity_item_list&page_type=pc&ss_tx=%s&page=1",
                encodedKeyword
            );

            log.debug("Step 2: Calling Ssadagu search API with keyword: {}", keyword);

            // API 호출 (쿠키 포함)
            Connection.Response response = Jsoup.connect(SEARCH_API_URL)
                    .userAgent(USER_AGENT)
                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("Origin", SSADAGU_BASE_URL)
                    .header("Referer", searchPageUrl)
                    .header("X-Requested-With", "XMLHttpRequest")  // AJAX 표시
                    .cookies(cookies)  // 받은 쿠키 전달
                    .requestBody(requestBody)
                    .ignoreContentType(true)
                    .maxBodySize(0)  // 바디 사이즈 제한 제거 (기본값 2MB)
                    .timeout(TIMEOUT_MS)
                    .method(Connection.Method.POST)
                    .execute();

            // JSON 응답 파싱 (인코딩 명시)
            String jsonResponse = response.body();
            log.debug("API Response (first 500 chars): {}", jsonResponse.substring(0, Math.min(500, jsonResponse.length())));

            JsonNode root = objectMapper.readTree(jsonResponse);

            if (!root.path("success").asBoolean()) {
                log.warn("Ssadagu search API returned success=false for keyword: {}", keyword);
                return null;
            }

            JsonNode dataArray = root.path("data");
            if (dataArray.isMissingNode() || !dataArray.isArray() || dataArray.isEmpty()) {
                log.warn("No search results from Ssadagu API for keyword: {}", keyword);
                return null;
            }

            // 첫 번째 상품의 HTML에서 URL 추출 (JSON에서 이미 문자열로 되어 있음)
            String firstProductHtml = dataArray.get(0).asText();
            log.debug("First product HTML length: {}", firstProductHtml.length());
            log.debug("First product HTML snippet: {}", firstProductHtml.substring(0, Math.min(300, firstProductHtml.length())));

            // Jsoup으로 HTML 파싱
            Document firstProduct = Jsoup.parse(firstProductHtml);

            // a 태그 중 href에 view.php가 포함된 것 찾기
            Elements allLinks = firstProduct.select("a");
            log.debug("Found {} <a> tags in HTML", allLinks.size());

            Element linkElement = null;
            for (Element link : allLinks) {
                String href = link.attr("href");
                if (href.contains("view.php")) {
                    linkElement = link;
                    log.debug("Found link with href: {}", href);
                    break;
                }
            }

            if (linkElement == null) {
                log.warn("Failed to extract product URL from search result");
                log.warn("HTML content: {}", firstProductHtml);
                return null;
            }

            String productUrl = linkElement.attr("href");

            // 상대 URL을 절대 URL로 변환
            if (!productUrl.startsWith("http")) {
                if (productUrl.startsWith("/")) {
                    productUrl = SSADAGU_BASE_URL + productUrl;
                } else {
                    productUrl = SSADAGU_BASE_URL + "/" + productUrl;
                }
            }

            log.info("Successfully extracted product URL: {}", productUrl);

            return productUrl;

        } catch (IOException e) {
            // IOException은 상위 메서드에서 처리하도록 throw
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while searching Ssadagu for keyword: {}", keyword, e);
            throw new IOException("Unexpected error: " + e.getMessage(), e);
        }
    }


    /**
     * 상품 상세 페이지에서 상품 정보 추출
     */
    private SsadaguProductDto extractProductInfo(Document doc, String productUrl, String category) {
        try {
            // 제목 추출
            String productName = extractProductName(doc);
            if (productName == null || productName.isBlank()) {
                log.warn("Failed to extract product name from: {}", productUrl);
                return null;
            }

            // 가격 추출
            Integer price = extractPrice(doc);

            // 별점 추출
            Double rating = extractRating(doc);

            // 이미지 URL 추출
            String imageUrl = extractImageUrl(doc);

            // 상품 정보 추출
            Map<String, String> productAttributes = extractProductAttributes(doc);

            // 리뷰 수는 현재 HTML에서 확인 불가 (null로 설정)
            Integer reviewCount = null;

            log.info("Successfully extracted product: {}", productName);

            return SsadaguProductDto.builder()
                    .productName(productName)
                    .productUrl(productUrl)
                    .price(price)
                    .rating(rating)
                    .reviewCount(reviewCount)
                    .imageUrl(imageUrl)
                    .category(category)
                    .productAttributes(productAttributes)
                    .build();

        } catch (Exception e) {
            log.error("Failed to extract product info from: {}", productUrl, e);
            return null;
        }
    }

    /**
     * 상품명 추출
     * 셀렉터: h1#kakaotitle
     */
    private String extractProductName(Document doc) {
        try {
            Element titleElement = doc.selectFirst("h1#kakaotitle");
            if (titleElement != null) {
                return titleElement.text().trim();
            }

            // 대안: class가 없는 h1 태그
            Elements h1Elements = doc.select("h1");
            if (!h1Elements.isEmpty()) {
                return h1Elements.first().text().trim();
            }

        } catch (Exception e) {
            log.debug("Failed to extract product name", e);
        }
        return null;
    }

    /**
     * 가격 추출
     * 셀렉터: span.gsItemPriceKWR
     */
    private Integer extractPrice(Document doc) {
        try {
            Element priceElement = doc.selectFirst("span.gsItemPriceKWR");
            if (priceElement != null) {
                String priceText = priceElement.text().trim();
                // 숫자만 추출 (쉼표, 공백 등 제거)
                String numericPrice = priceText.replaceAll("[^0-9]", "");
                if (!numericPrice.isBlank()) {
                    return Integer.parseInt(numericPrice);
                }
            }

            // 대안: span.price 태그
            Elements priceElements = doc.select("span.price");
            if (!priceElements.isEmpty()) {
                String priceText = priceElements.first().text().trim();
                String numericPrice = priceText.replaceAll("[^0-9]", "");
                if (!numericPrice.isBlank()) {
                    return Integer.parseInt(numericPrice);
                }
            }

        } catch (Exception e) {
            log.debug("Failed to extract price", e);
        }
        return null;
    }

    /**
     * 별점 추출
     * 셀렉터: a.start 안의 별 이미지 개수 계산
     * icon_star.svg = 1점, icon_star_half.svg = 0.5점
     */
    private Double extractRating(Document doc) {
        try {
            Element starContainer = doc.selectFirst("a.start");
            if (starContainer == null) {
                log.debug("Star rating container not found");
                return null;
            }

            Elements starImages = starContainer.select("img");
            if (starImages.isEmpty()) {
                return null;
            }

            double totalRating = 0.0;

            for (Element img : starImages) {
                String src = img.attr("src");
                String alt = img.attr("alt");

                boolean isHalf = src.contains("half") || alt.contains("반");
                if (src.contains("icon_star.svg") && !isHalf) {
                    totalRating += 1.0;
                } else if (src.contains("icon_star_half.svg") || isHalf) {
                    totalRating += 0.5;
                }
            }

            // 0점이면 null 반환
            if (totalRating == 0.0) {
                return null;
            }

            log.debug("Extracted rating: {}", totalRating);
            return totalRating;

        } catch (Exception e) {
            log.debug("Failed to extract rating", e);
        }
        return null;
    }

    /**
     * 이미지 URL 추출
     * 셀렉터: img.slick-parent-wrap 또는 img#img_translate_thumbnail_0
     */
    private String extractImageUrl(Document doc) {
        try {
            // 우선순위 1: slick-parent-wrap 클래스
            Element imgElement = doc.selectFirst("img.slick-parent-wrap");
            if (imgElement != null) {
                String src = imgElement.attr("src");
                if (!src.isBlank()) {
                    return src;
                }
            }

            // 우선순위 2: img_translate_thumbnail_0 ID
            imgElement = doc.selectFirst("img#img_translate_thumbnail_0");
            if (imgElement != null) {
                String src = imgElement.attr("src");
                if (!src.isBlank()) {
                    return src;
                }
            }

            // 대안: 첫 번째 상품 이미지
            Elements imgElements = doc.select("img[src*='alicdn'], img[src*='jpg'], img[src*='png']");
            if (!imgElements.isEmpty()) {
                return imgElements.first().attr("src");
            }

        } catch (Exception e) {
            log.debug("Failed to extract image URL", e);
        }
        return null;
    }

    /**
     * 상품 정보 추출
     * 셀렉터: div.pro-info-boxs > div.pro-info-item
     * 각 항목에서 제목(pro-info-title)과 내용(pro-info-info) 추출
     */
    private Map<String, String> extractProductAttributes(Document doc) {
        Map<String, String> attributes = new LinkedHashMap<>();

        try {
            // 여러 가능한 셀렉터 시도
            Element attributesBox = doc.selectFirst("div.pro-info-boxs#productAttributes");

            if (attributesBox == null) {
                log.warn("Product attributes container #productAttributes not found, trying alternative selectors");

                // 대안 1: ID 없이 클래스만
                attributesBox = doc.selectFirst("div.pro-info-boxs");

                if (attributesBox == null) {
                    log.warn("Product attributes container .pro-info-boxs also not found");
                    return attributes;
                }
            }

            Elements items = attributesBox.select("div.pro-info-item");
            log.info("Found {} product attribute items", items.size());

            for (Element item : items) {
                // hidden 클래스가 있는 항목도 포함 (모든 정보 수집)
                Element titleElement = item.selectFirst("div.pro-info-title");
                Element infoElement = item.selectFirst("div.pro-info-info");

                if (titleElement != null && infoElement != null) {
                    String title = titleElement.text().trim();
                    String info = infoElement.text().trim();

                    if (!title.isBlank() && !info.isBlank()) {
                        attributes.put(title, info);
                        log.info("Added attribute: {} = {}", title, info);
                    }
                }
            }

            log.info("Successfully extracted {} product attributes", attributes.size());

        } catch (Exception e) {
            log.error("Failed to extract product attributes", e);
        }

        return attributes;
    }
}
