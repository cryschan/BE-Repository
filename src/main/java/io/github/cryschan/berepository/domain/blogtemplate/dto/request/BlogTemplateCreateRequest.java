package io.github.cryschan.berepository.domain.blogtemplate.dto.request;

import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public record BlogTemplateCreateRequest(
        @Size(max = 100)
        @Schema(example = "패션 블로그 템플릿", description = "비우면 '{사용자명}의 템플릿'으로 저장됩니다.")
        String title,

        @NotEmpty
        @Schema(example = "[\"fashion\", \"shoes\"]")
        List<@NotBlank String> categories,

        @NotEmpty
        @Schema(example = "[\"naver\", \"tistory\"]")
        List<@NotBlank String> platforms,

        @NotBlank
        @Schema(example = "https://example.com")
        String shopUrl,

        @NotNull
        @Schema(example = "true")
        Boolean includeImages,

        @Min(0)
        @Max(10)
        @Schema(example = "1", minimum = "1", maximum = "10", description = "includeImages=true일 때 1~10, false면 0")
        int imageCount,

        @Min(0)
        @Schema(example = "500", description = "500 단위 값만 허용 (500, 1000, ...)")
        int charLimit,

        @NotNull
        @JsonFormat(pattern = "HH:mm:ss")
        @Schema(example = "09:00:00", description = "정각만 허용 (HH:mm:ss)")
        LocalTime dailyPostTime
) {

    @AssertTrue(message = "이미지 포함 시 imageCount는 1~10이어야 하며, 미포함 시 0이어야 합니다")
    public boolean isValidImageCount() {
        if (Boolean.TRUE.equals(includeImages)) {
            return imageCount >= 1 && imageCount <= 10;
        }
        return imageCount == 0;
    }

    @AssertTrue(message = "글자 수 제한은 500의 양수 배수여야 합니다 (예: 500, 1000, 1500)")
    public boolean isValidCharLimit() {
        return charLimit > 0 && charLimit % 500 == 0;
    }

    @AssertTrue(message = "포스팅 시간은 정각이어야 합니다 (분/초는 0)")
    public boolean isValidDailyPostTime() {
        return dailyPostTime != null
                && dailyPostTime.getMinute() == 0
                && dailyPostTime.getSecond() == 0
                && dailyPostTime.getNano() == 0;
    }

    public BlogTemplate toEntity(Long userId, String username) {
        String safeTitle = (title == null || title.isBlank()) ? username + "의 템플릿" : title;
        return BlogTemplate.builder()
                .title(safeTitle)
                .categories(new ArrayList<>(categories))
                .platforms(new ArrayList<>(platforms))
                .shopUrl(shopUrl)
                .includeImages(includeImages)
                .imageCount(imageCount)
                .charLimit(charLimit)
                .dailyPostTime(dailyPostTime)
                .userId(userId)
                .build();
    }
}
