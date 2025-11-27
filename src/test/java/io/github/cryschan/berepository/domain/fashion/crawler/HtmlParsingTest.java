package io.github.cryschan.berepository.domain.fashion.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class HtmlParsingTest {

    @Test
    void testRealSsadaguPage() throws Exception {
        String url = "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=688124293631";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

        Document doc = Jsoup.connect(url)
                .userAgent(userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .timeout(10000)
                .get();

        System.out.println("=== Document fetched successfully ===");

        // 상품정보 추출 테스트
        Map<String, String> attributes = new LinkedHashMap<>();

        Element attributesBox = doc.selectFirst("div.pro-info-boxs#productAttributes");

        if (attributesBox == null) {
            System.out.println("❌ Product attributes container #productAttributes not found");

            // 대안 시도
            attributesBox = doc.selectFirst("div.pro-info-boxs");
            if (attributesBox == null) {
                System.out.println("❌ Product attributes container .pro-info-boxs also not found");
                return;
            } else {
                System.out.println("✅ Found .pro-info-boxs (without ID)");
            }
        } else {
            System.out.println("✅ Found div.pro-info-boxs#productAttributes");
        }

        Elements items = attributesBox.select("div.pro-info-item");
        System.out.println("Found " + items.size() + " product attribute items");

        for (Element item : items) {
            Element titleElement = item.selectFirst("div.pro-info-title");
            Element infoElement = item.selectFirst("div.pro-info-info");

            if (titleElement != null && infoElement != null) {
                String title = titleElement.text().trim();
                String info = infoElement.text().trim();

                if (!title.isBlank() && !info.isBlank()) {
                    attributes.put(title, info);
                }
            }
        }

        System.out.println("\n=== Extracted Product Attributes (" + attributes.size() + ") ===");
        attributes.forEach((key, value) -> System.out.println(key + " = " + value));

        if (attributes.isEmpty()) {
            System.out.println("❌ No attributes extracted!");
        } else {
            System.out.println("✅ Successfully extracted " + attributes.size() + " attributes");
        }
    }
}
