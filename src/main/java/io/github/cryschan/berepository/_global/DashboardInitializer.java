package io.github.cryschan.berepository._global;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cryschan.berepository.domain.dashboard.entity.Dashboard;
import io.github.cryschan.berepository.domain.dashboard.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 애플리케이션 시작 시 전날 대시보드 더미 데이터를 생성하는 클래스
 * 증감률 계산 테스트를 위해 사용됩니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Order(4)  // Blog, User, BlogTemplate 초기화 이후 실행
public class DashboardInitializer implements CommandLineRunner {

    private final DashboardRepository dashboardRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=".repeat(80));
        log.info("Starting Dashboard Dummy Data Initialization...");
        log.info("=".repeat(80));

        try {
            createYesterdayDashboardIfNotExists();

            log.info("=".repeat(80));
            log.info("Dashboard Dummy Data Initialization Completed Successfully");
            log.info("=".repeat(80));
        } catch (Exception e) {
            log.error("=".repeat(80));
            log.error("Dashboard Dummy Data Initialization Failed!", e);
            log.error("=".repeat(80));
            // 초기화 실패해도 애플리케이션은 계속 실행되도록 예외를 던지지 않음
            log.warn("Dashboard dummy data initialization failed, but continuing application startup...");
        }
    }

    private void createYesterdayDashboardIfNotExists() {
        log.debug("Checking if yesterday dashboard data exists...");

        // 전날 날짜 계산
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay();

        // 이미 데이터가 있는지 확인
        if (dashboardRepository.findByDate(yesterday).isPresent()) {
            log.info("[SKIP] Yesterday dashboard data already exists. Date: {}", yesterday);
            return;
        }

        log.debug("Yesterday dashboard data does not exist. Creating dummy data for date: {}", yesterday);

        try {
            // 더미 데이터 생성
            Dashboard dummyDashboard = createDummyDashboard(yesterday);
            dashboardRepository.save(dummyDashboard);
            log.info("[CREATED] Yesterday dashboard dummy data created successfully for date: {}", yesterday);
        } catch (Exception e) {
            log.error("Failed to create yesterday dashboard data for date: {}", yesterday, e);
            throw e;
        }
    }

    /**
     * 더미 대시보드 데이터 생성
     * 증감률 계산 테스트를 위해 의미 있는 값들을 설정
     */
    private Dashboard createDummyDashboard(LocalDateTime date) {
        try {
            // 더미 카테고리 분포 데이터
            Map<String, Long> categoryDistribution = new HashMap<>();
            categoryDistribution.put("패션", 5L);
            categoryDistribution.put("뷰티", 3L);
            categoryDistribution.put("의류", 2L);
            String categoryDistributionJson = objectMapper.writeValueAsString(categoryDistribution);

            // 더미 플랫폼 사용 데이터
            Map<String, Long> platformUsage = new HashMap<>();
            platformUsage.put("네이버", 8L);
            platformUsage.put("카카오", 5L);
            platformUsage.put("인스타그램", 2L);
            String platformUsageJson = objectMapper.writeValueAsString(platformUsage);

            // 더미 오늘 블로그 리스트 (빈 리스트)
            List<Map<String, Object>> todayBlogList = List.of();
            String todayBlogListJson = objectMapper.writeValueAsString(todayBlogList);

            // 더미 데이터 값 설정 (증감률 계산 테스트를 위해)
            return Dashboard.builder()
                    .date(date)
                    .adminUserId(null)  // 더미 데이터이므로 null
                    .activeUserCount(8)  // 활성 사용자 수 (오늘과 비교 가능하도록)
                    .todayBlogCount(3)   // 어제 생성된 글 수 (0이 아닌 값)
                    .totalBlogCount(15)  // 전체 블로그 수
                    .categoryDistribution(categoryDistributionJson)
                    .platformUsage(platformUsageJson)
                    .todayBlogList(todayBlogListJson)
                    .totalTokenUsage(5000L)  // 사용 토큰 수 (0이 아닌 값)
                    .build();
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize dummy dashboard data to JSON", e);
            throw new RuntimeException("더미 대시보드 데이터 생성 실패", e);
        }
    }
}

