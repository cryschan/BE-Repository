package io.github.cryschan.berepository.domain.fashion.crawler;

import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 싸다구(Ssadagu) 사이트에서 상품 정보를 크롤링하는 크롤러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SsadaguCrawler {

    private static final String SSADAGU_BASE_URL = "https://ssadagu.kr";
    private static final String SEARCH_URL_PATTERN = SSADAGU_BASE_URL + "/shop/search.php?ss_tx=%s";
    private static final int TIMEOUT_MS = 10000;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    // 카테고리별 대표 상품 URL (하드코딩)
    // TODO: 추후 Selenium/Playwright로 실제 검색 기능 구현
    // TODO: 실제 싸다구 상품 URL로 업데이트 필요 (현재는 구두 URL을 기본값으로 사용)
    private static final Map<String, String> FALLBACK_PRODUCT_URLS = Map.ofEntries(
            Map.entry("구두", "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=688124293631"),
            Map.entry("패딩", "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=688124293631"),  // 구두로 대체
            Map.entry("숏패딩", "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=688124293631"),  // 구두로 대체
            Map.entry("코트", "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=688124293631")  // 구두로 대체
    );

    /**
     * 카테고리로 상품을 검색하고 첫 번째 상품의 정보를 반환
     *
     * NOTE: 싸다구 검색 페이지가 JavaScript 렌더링을 사용하여 Jsoup으로 크롤링 불가능.
     * 현재는 하드코딩된 대표 상품 URL을 사용하며, 추후 Selenium/Playwright로 개선 예정.
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
            // 1단계: 폴백 URL 찾기 (하드코딩된 대표 상품)
            String productUrl = findFallbackProductUrl(category);

            if (productUrl == null) {
                log.warn("No fallback product URL found for category: {}. Trying generic search fallback.", category);
                // 기본 대표 상품 사용 (구두)
                productUrl = FALLBACK_PRODUCT_URLS.get("구두");
            }

            log.info("Using fallback product URL for category '{}': {}", category, productUrl);

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
     * 카테고리에 맞는 폴백 상품 URL 찾기
     * 정확한 매칭이 없으면 부분 매칭 시도
     */
    private String findFallbackProductUrl(String category) {
        // 1. 정확한 매칭
        if (FALLBACK_PRODUCT_URLS.containsKey(category)) {
            return FALLBACK_PRODUCT_URLS.get(category);
        }

        // 2. 부분 매칭 (예: "숏패딩/헤비 아우터" -> "숏패딩")
        for (Map.Entry<String, String> entry : FALLBACK_PRODUCT_URLS.entrySet()) {
            if (category.contains(entry.getKey())) {
                log.debug("Found partial match: '{}' in category '{}'", entry.getKey(), category);
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * 검색 결과 페이지에서 첫 번째 상품 URL 추출
     */
    private String extractFirstProductUrl(Document searchDoc) {
        try {
            // 1. view.php 링크 찾기 (싸다구 상품 페이지)
            Elements viewLinks = searchDoc.select("a[href*='view.php']");
            if (!viewLinks.isEmpty()) {
                String href = viewLinks.first().attr("href");
                log.debug("Found view.php link: {}", href);
                return makeAbsoluteUrl(href);
            }

            // 2. data-num_iid 속성을 가진 요소에서 platform과 num_iid 추출
            Elements dataElements = searchDoc.select("[data-num_iid], [data-platform]");
            if (!dataElements.isEmpty()) {
                for (Element elem : dataElements) {
                    String numIid = elem.attr("data-num_iid");
                    String platform = elem.attr("data-platform");

                    if (!numIid.isBlank() && !platform.isBlank()) {
                        String url = String.format("%s/shop/view.php?platform=%s&num_iid=%s",
                                SSADAGU_BASE_URL, platform, numIid);
                        log.debug("Constructed URL from data attributes: {}", url);
                        return url;
                    }
                }
            }

            // 3. 기존 패턴들 시도
            Elements productLinks = searchDoc.select(
                    "a[href*='/shop/item.php'], " +
                    "a[href*='/product/'], " +
                    "a[href*='item.php?it_id'], " +
                    "div.item a[href], " +
                    "div.product a[href], " +
                    "li.item a[href]"
            );

            if (!productLinks.isEmpty()) {
                String href = productLinks.first().attr("href");
                return makeAbsoluteUrl(href);
            }

            log.debug("No product links found in search results");
            return null;

        } catch (Exception e) {
            log.error("Failed to extract first product URL", e);
            return null;
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
     * 상대 URL을 절대 URL로 변환
     */
    private String makeAbsoluteUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        // 이미 절대 URL인 경우
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        // 상대 URL을 절대 URL로 변환
        if (url.startsWith("/")) {
            return SSADAGU_BASE_URL + url;
        } else {
            return SSADAGU_BASE_URL + "/" + url;
        }
    }
}
