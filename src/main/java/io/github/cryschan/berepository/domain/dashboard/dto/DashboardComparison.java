package io.github.cryschan.berepository.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 어제 대비 증감률 및 변화량 정보를 담는 DTO
 */
@Getter
@AllArgsConstructor
public class DashboardComparison {

    
    // 오늘 생성된 글 수 증감률 (%)
    private Double todayBlogCountChangeRate;

    //오늘 생성된 글 수 변화량
    private Integer todayBlogCountChange;

    // 활성 사용자 수 증감률 (%)
    private Double activeUserCountChangeRate;

    // 활성 사용자 수 변화량
    private Integer activeUserCountChange;

    // 사용 토큰 수 증감률 (%)
    private Double totalTokenUsageChangeRate;

    // 사용 토큰 수 변화량
    private Long totalTokenUsageChange;

}
