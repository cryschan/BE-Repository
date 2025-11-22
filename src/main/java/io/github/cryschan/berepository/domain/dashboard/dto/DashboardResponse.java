package io.github.cryschan.berepository.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class DashboardResponse {

    private Integer activeUserCount;
    private Integer todayBlogCount;
    private Integer totalBlogCount;

    private Map<String, Long> categoryDistribution;
    private Map<String, Long> platformUsage;

    private List<TodayBlogItem> todayBlogItemList;
    
    private Long totalTokenUsage;  // 사용한 토큰 수

}
