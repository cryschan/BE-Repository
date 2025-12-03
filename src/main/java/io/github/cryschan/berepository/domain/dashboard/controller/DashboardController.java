package io.github.cryschan.berepository.domain.dashboard.controller;

import io.github.cryschan.berepository.domain.dashboard.dto.DashboardResponse;
import io.github.cryschan.berepository.domain.dashboard.service.DashboardService;
import io.github.cryschan.berepository.domain.user.exception.UserException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Tag(name = "대시보드", description = "대시보드 API")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 대시보드 데이터 조회
     */
    @Operation(summary = "대시보드 조회", description = "대시보드 데이터를 조회합니다 (관리자만 접근 가능)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "대시보드 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DashboardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "인증이 필요합니다",
                                                "status": 401,
                                                "code": "U004",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (관리자만 접근 가능)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "대시보드에 대한 접근 권한이 없습니다",
                                                "status": 403,
                                                "code": "U005",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(Principal principal) {
        Long userId = extractUserId(principal);
        DashboardResponse response = dashboardService.getDashboardData(userId);
        return ResponseEntity.ok(response);
    }

    private Long extractUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw UserException.unauthorized("인증이 필요합니다");
        }
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            throw UserException.unauthorized("유효하지 않은 사용자 인증 정보입니다");
        }
    }

}
