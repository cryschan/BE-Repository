package io.github.cryschan.berepository.domain.blogtemplate.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public record BlogTemplateUpdateRequest(
        @NotBlank
        @Size(max = 100)
        String title,

        @NotEmpty
        List<@NotBlank String> categories,

        @NotEmpty
        List<@NotBlank String> platforms,

        @NotBlank
        String shopUrl,

        @NotNull
        Boolean includeImages,

        @Min(0)
        int imageCount,

        @Min(0)
        int charLimit,

        @NotNull
        LocalTime dailyPostTime
) {

    public List<String> categoriesCopy() {
        return new ArrayList<>(categories);
    }

    public List<String> platformsCopy() {
        return new ArrayList<>(platforms);
    }
}

