package io.github.cryschan.berepository.domain.fashion.service;

import io.github.cryschan.berepository.domain.fashion.dto.response.KeywordResponseDto;
import io.github.cryschan.berepository.domain.fashion.dto.response.MagazineResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;

// 무신사 매거진의 인기 글 키워드를 가져온다.
@Slf4j
@Service
public class MagazineService {

    private final RestClient restClient;

    public MagazineService(@Qualifier("musinsaContentRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    // 무신사 매거진 api를 호출하고 keyword를 응답 받는다.
    public List<KeywordResponseDto> popularAll() {
        try {
            log.debug("Fetching popular magazine keywords from Musinsa API");

            MagazineResponseDto magazineData = restClient.get()
                    .uri("/api2/content/musinsa-content/v1/contents/scored-list?scoreType=CONTENT_POPULARITY_SCORE&contentCategoryCode=001001002&size=4")
                    .retrieve()
                    .body(new ParameterizedTypeReference<MagazineResponseDto>() {
                    });

            if (magazineData == null || magazineData.data() == null || magazineData.data().list() == null) {
                log.warn("Received null or empty response from Musinsa magazine API");
                return Collections.emptyList();
            }

            log.info("Successfully fetched {} magazine keywords", magazineData.data().list().size());
            return magazineData.data().list();

        } catch (RestClientException e) {
            log.error("Failed to fetch magazine keywords from Musinsa API", e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error while fetching magazine keywords", e);
            return Collections.emptyList();
        }
    }
}
