package io.github.cryschan.berepository.domain.fashion.dto.response;

import java.time.LocalDateTime;

// 무신사 의류 키워드
public record KeywordResponseDto(
        Long keywordId,
        String landingUrl,
        String thumbnailUrl,
        String title,
        long viewCount,
        float score,
        LocalDateTime startDate
        ) {
}
