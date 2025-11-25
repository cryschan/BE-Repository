package io.github.cryschan.berepository.domain.dashboard.scheduler;

import io.github.cryschan.berepository.domain.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 대시보드 데이터를 주기적으로 갱신하는 스케줄러
 * 매일 자정에 전날 대시보드 데이터를 계산하여 DB에 저장합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardScheduler {

    private final DashboardService dashboardService;

    /**
     * 매일 자정(00:00:00)에 실행되어 전날 대시보드 데이터를 저장
     * cron 표현식: "0 0 0 * * ?" (초 분 시 일 월 요일)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateDailyDashboard() {
        // 전날 날짜의 00:00:00으로 설정
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay();
        log.info("Starting daily dashboard update for date: {}", yesterday);

        try {
            dashboardService.updateDashboardData(yesterday, null);
            log.info("Daily dashboard update completed successfully for date: {}", yesterday);
        } catch (Exception e) {
            log.error("Failed to update dashboard data for date: {}", yesterday, e);
        }
    }
}
