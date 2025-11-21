package io.github.cryschan.berepository.domain.faqs.dto.response;

import io.github.cryschan.berepository.domain.faqs.entity.Faqs;

import java.time.LocalDateTime;

public record FaqsResponse(
        Long id,
        String question,
        String answer,
        Integer sortOrder,
        LocalDateTime createdAt
) {

    public static FaqsResponse from(Faqs faqs) {
        return new FaqsResponse(
                faqs.getId(),
                faqs.getQuestion(),
                faqs.getAnswer(),
                faqs.getSortOrder(),
                faqs.getCreatedAt()
        );
    }
}
