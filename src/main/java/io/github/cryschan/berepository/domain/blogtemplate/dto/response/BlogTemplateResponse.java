package io.github.cryschan.berepository.domain.blogtemplate.dto.response;

import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record BlogTemplateResponse(
        Long id,
        String title,
        LocalDateTime createdAt,
        List<String> categories,
        List<String> platforms,
        String shopUrl,
        boolean includeImages,
        int imageCount,
        int charLimit,
        LocalTime dailyPostTime
) {

    public static BlogTemplateResponse from(BlogTemplate template) {
        return new BlogTemplateResponse(
                template.getId(),
                template.getTitle(),
                template.getCreatedAt(),
                template.getCategories(),
                template.getPlatforms(),
                template.getShopUrl(),
                template.isIncludeImages(),
                template.getImageCount(),
                template.getCharLimit(),
                template.getDailyPostTime()
        );
    }
}
