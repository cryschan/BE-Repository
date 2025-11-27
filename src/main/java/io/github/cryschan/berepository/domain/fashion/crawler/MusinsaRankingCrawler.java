package io.github.cryschan.berepository.domain.fashion.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cryschan.berepository.domain.fashion.dto.response.MusinsaRankingLinkDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MusinsaRankingCrawler {

    private static final String RANKING_API = "/api2/hm/web/v5/pans/ranking/sections/199"
            + "?storeCode=musinsa&gf=A&ageBand=AGE_BAND_ALL&period=DAILY"
            + "&eventPeriod=BASIC_REALTIME&categoryCode=000&page=1&startRank=1&offset=20";
    private static final String TARGET_SECTION_NAME = "ranking_goods_list";

    private final RestClient musinsaRankingRestClient;
    private final ObjectMapper objectMapper;

    public MusinsaRankingCrawler(
            @Qualifier("musinsaRankingRestClient") RestClient musinsaRankingRestClient,
            ObjectMapper objectMapper
    ) {
        this.musinsaRankingRestClient = musinsaRankingRestClient;
        this.objectMapper = objectMapper;
    }

    public List<MusinsaRankingLinkDto> fetchTopLinks(int limit) {
        String responseBody = musinsaRankingRestClient.get()
                .uri(RANKING_API)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode dataNode = root.path("data");
            if (dataNode.isMissingNode()) {
                log.warn("Musinsa ranking API returned no data field.");
                return List.of();
            }

            Iterator<JsonNode> amplitudeNodes = dataNode.findValues("amplitude").iterator();
            List<MusinsaRankingLinkDto> links = new ArrayList<>();
            Set<String> seenUrls = new HashSet<>();  // 중복 URL 체크용

            while (amplitudeNodes.hasNext()) {
                JsonNode amplitudeNode = amplitudeNodes.next();
                JsonNode payload = amplitudeNode.path("payload");
                if (!TARGET_SECTION_NAME.equals(payload.path("section_name").asText())) {
                    continue;
                }

                String url = payload.path("url").asText();
                if (!url.contains("/products/")) {
                    continue;
                }
                String rankText = payload.path("index").asText();
                if (url.isBlank() || rankText.isBlank()) {
                    continue;
                }

                // 중복 URL 제거: 같은 URL이면 스킵
                if (seenUrls.contains(url)) {
                    log.debug("Skipping duplicate URL: {}", url);
                    continue;
                }

                try {
                    int rank = Integer.parseInt(rankText);
                    links.add(new MusinsaRankingLinkDto(rank, url));
                    seenUrls.add(url);  // 처리한 URL 기록
                } catch (NumberFormatException e) {
                    log.debug("Skipping payload due to invalid rank: {}", rankText);
                }
            }

            // 랭킹 순으로 정렬 후 상위 N개만 반환
            links.sort(Comparator.comparingInt(MusinsaRankingLinkDto::rank));
            List<MusinsaRankingLinkDto> result = links.size() > limit ? links.subList(0, limit) : links;

            log.info("Fetched {} unique products from Musinsa ranking (requested: {})", result.size(), limit);
            return result;
        } catch (Exception e) {
            log.error("Failed to parse Musinsa ranking API response", e);
            throw new IllegalStateException("Cannot parse Musinsa ranking", e);
        }
    }
}
