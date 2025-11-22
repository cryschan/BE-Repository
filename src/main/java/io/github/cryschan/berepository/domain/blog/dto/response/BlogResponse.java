package io.github.cryschan.berepository.domain.blog.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.cryschan.berepository.domain.blog.entity.Blog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "블로그 응답")
@Getter
@Builder
public class BlogResponse {

    @Schema(description = "블로그 ID", example = "1")
    private Long id;

    @Schema(description = "블로그 제목", example = "여름 남성 반팔 티셔츠 추천")
    private String title;

    @Schema(description = "블로그 내용", example = "# 여름 남성 반팔 티셔츠 추천\n\n여름철 필수 아이템...")
    private String content;

    @Schema(description = "카테고리", example = "남성 의류")
    private String category;

    @Schema(description = "이미지 URL", example = "https://example.com/images/tshirt.jpg")
    private String imgUrl;

    @Schema(description = "블로그 템플릿 ID", example = "template-001")
    private String blogTemplateId;

    @Schema(description = "생성 일시", example = "2025-11-20T20:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-11-20T20:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "오늘 작성 여부", example = "true")
    @Getter(AccessLevel.NONE)  // Lombok getter 생성 제외
    @JsonProperty("isToday")  // JSON 필드명 명시
    private boolean isToday;

    public static BlogResponse from(Blog blog) {
        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .content(blog.getContent())
                .category(blog.getCategory())
                .imgUrl(blog.getImgUrl())
                .blogTemplateId(blog.getBlogTemplateId())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .isToday(isCreatedToday(blog.getCreatedAt()))
                .build();
    }

    private static boolean isCreatedToday(LocalDateTime createdAt) {
        if (createdAt == null) {
            return false;
        }
        return createdAt.toLocalDate().equals(LocalDate.now());
    }
}