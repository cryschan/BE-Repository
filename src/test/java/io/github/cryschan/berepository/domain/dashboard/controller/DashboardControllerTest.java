package io.github.cryschan.berepository.domain.dashboard.controller;

import io.github.cryschan.berepository._global.exception.handler.GlobalExceptionHandler;
import io.github.cryschan.berepository.domain.dashboard.dto.DashboardResponse;
import io.github.cryschan.berepository.domain.dashboard.service.DashboardService;
import io.github.cryschan.berepository.domain.user.exception.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardController 테스트")
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("대시보드 조회 테스트")
    class GetDashboardTest {

        @Test
        @DisplayName("401 인증 실패: 토큰 없이 호출 시 인증 오류 발생")
        void getDashboard_Unauthorized_NoToken() throws Exception {
            // given - Principal이 null인 경우 (토큰 없이 호출)
            // when & then
            mockMvc.perform(get("/api/dashboard")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("U004"))
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("인증이 필요합니다"));
        }

        @Test
        @DisplayName("403 권한 없음: USER 권한으로 호출 시 권한 오류 발생")
        void getDashboard_Forbidden_UserRole() throws Exception {
            // given
            Long userId = 1L;
            Principal principal = () -> userId.toString();

            given(dashboardService.getDashboardData(userId))
                    .willThrow(UserException.accessDenied("대시보드"));

            // when & then
            mockMvc.perform(get("/api/dashboard")
                            .principal(principal)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("U005"))
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.message").value("대시보드에 대한 접근 권한이 없습니다"));
        }

        @Test
        @DisplayName("200 성공: ADMIN 권한으로 호출 시 대시보드 데이터 반환")
        void getDashboard_Success_AdminRole() throws Exception {
            // given
            Long adminUserId = 1L;
            Principal principal = () -> adminUserId.toString();

            Map<String, Long> categoryDistribution = new java.util.HashMap<>();
            categoryDistribution.put("패션", 10L);
            categoryDistribution.put("의류", 8L);

            Map<String, Long> platformUsage = new java.util.HashMap<>();
            platformUsage.put("네이버", 50L);
            platformUsage.put("카카오", 30L);

            DashboardResponse dashboardResponse = new DashboardResponse(
                    10,  // activeUserCount
                    5,   // todayBlogCount
                    100, // totalBlogCount
                    categoryDistribution,  // categoryDistribution
                    platformUsage,  // platformUsage
                    List.of(),  // todayBlogItemList
                    1000L  // totalTokenUsage
            );

            given(dashboardService.getDashboardData(adminUserId)).willReturn(dashboardResponse);

            // when & then
            mockMvc.perform(get("/api/dashboard")
                            .principal(principal)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.activeUserCount").value(10))
                    .andExpect(jsonPath("$.todayBlogCount").value(5))
                    .andExpect(jsonPath("$.totalBlogCount").value(100))
                    .andExpect(jsonPath("$.categoryDistribution.패션").value(10))
                    .andExpect(jsonPath("$.categoryDistribution.의류").value(8))
                    .andExpect(jsonPath("$.platformUsage.네이버").value(50))
                    .andExpect(jsonPath("$.platformUsage.카카오").value(30))
                    .andExpect(jsonPath("$.totalTokenUsage").value(1000));
        }
    }
}
