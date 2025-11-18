package io.github.cryschan.berepository.domain.musinsa.controller;

import io.github.cryschan.berepository.domain.musinsa.dto.response.KeywordResponseDto;
import io.github.cryschan.berepository.domain.musinsa.dto.response.MagazineResponseDto;
import io.github.cryschan.berepository.domain.musinsa.service.MagazineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 무신사 매거진에서 인기 글 url 크롤링해오기
@RequiredArgsConstructor
@RequestMapping("/api/keyword")
@RestController
public class MusinsaController {

    private final MagazineService magazineService;

    // https://content.musinsa.com/api2/content/musinsa-content/v1/contents/scored-list?scoreType=CONTENT_POPULARITY_SCORE&contentCategoryCode=001001002&size=4
    // 무신사 매거진글을 크롤링해온다.
    // 1. restClient 를 통해서 url을 get을 호출한다.
    // 2. 호출한 url에서 json을 받는다.
    // 3. attributeDictionaryName, landingUrl, title, viewcount, score, displayStartDate를 응답받는다.
    // 3.1 응답형식은 data/list/[content] 형식이다. []은 리스트이다.
    // 4. 컨트롤러 -> 서비스 -> dto(requestDto, responseDto)
    // 5. 엔티티 - dto 변경은 따로 mapper 클래스를 활용한다. (미정)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/fashion")
    public List<KeywordResponseDto> magazine() {
        return magazineService.popularAll();
    }
}
