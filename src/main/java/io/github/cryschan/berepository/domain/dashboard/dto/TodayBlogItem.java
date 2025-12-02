package io.github.cryschan.berepository.domain.dashboard.dto;

import io.github.cryschan.berepository.domain.blog.entity.BlogPublishStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TodayBlogItem {

    private String title;
    private String platform;
    private LocalDateTime createdAt;
    private String username;  // 템플릿 생성자 username
    private String category;
    private BlogPublishStatus publishStatus;  // 발행 상태
    private String failureReason;  // 발행 실패 사유 (실패한 경우에만)

}
