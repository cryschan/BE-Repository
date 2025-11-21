package io.github.cryschan.berepository.domain.fashion.service;

import io.github.cryschan.berepository.domain.fashion.crawler.MusinsaRankingCrawler;
import io.github.cryschan.berepository.domain.fashion.dto.response.MusinsaRankingLinkDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FashionCrawlerService {

    private static final int DEFAULT_LIMIT = 5;

    private final MusinsaRankingCrawler musinsaRankingCrawler;

    public List<MusinsaRankingLinkDto> fetchTrendingLinks(Integer limit) {
        int resolvedLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : limit;
        return musinsaRankingCrawler.fetchTopLinks(resolvedLimit);
    }
}
