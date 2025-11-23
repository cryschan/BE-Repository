package io.github.cryschan.berepository.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TodayBlogItem {

    private String title;
    private String platform;
    private LocalDateTime createdAt;

}
