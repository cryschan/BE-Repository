package io.github.cryschan.berepository.domain.fashion.service;

import io.github.cryschan.berepository.domain.fashion.dto.response.KeywordResponseDto;
import io.github.cryschan.berepository.domain.fashion.dto.response.MagazineResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

// 무신사 매거진의 인기 글 키워드를 가져온다.
@RequiredArgsConstructor
@Service
public class MagazineService {

    private final RestClient restClient;

    // 무신사 매거진 api를 호출하고 keyword를 응답 받는다.
    public List<KeywordResponseDto> popularAll() {
        MagazineResponseDto magazineData = restClient.get()
                .uri("/api2/content/musinsa-content/v1/contents/scored-list?scoreType=CONTENT_POPULARITY_SCORE&contentCategoryCode=001001002&size=4")
                .retrieve()
                .body(new ParameterizedTypeReference<MagazineResponseDto>() {
                });

        return magazineData.data().list();
    }
}
