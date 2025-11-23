package io.github.cryschan.berepository.domain.dashboard.controller;

import io.github.cryschan.berepository.domain.dashboard.dto.DashboardResponse;
import io.github.cryschan.berepository.domain.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대시보드 관련 컨트롤러
 * 대시보드 데이터 조회 기능을 처리합니다.
 */
@Tag(name = "대시보드", description = "대시보드 API")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 대시보드 데이터 조회
     */
    @Operation(summary = "대시보드 조회", description = "대시보드 데이터를 조회합니다")
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        DashboardResponse response = dashboardService.getDashboardData();
        return ResponseEntity.ok(response);
    }

}
