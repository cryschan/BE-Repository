package io.github.cryschan.berepository.domain.dashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cryschan.berepository.domain.blog.entity.Blog;
import io.github.cryschan.berepository.domain.blog.repository.BlogRepository;
import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.repository.BlogTemplateRepository;
import io.github.cryschan.berepository.domain.dashboard.dto.DashboardResponse;
import io.github.cryschan.berepository.domain.dashboard.dto.TodayBlogItem;
import io.github.cryschan.berepository.domain.dashboard.entity.Dashboard;
import io.github.cryschan.berepository.domain.dashboard.repository.DashboardRepository;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import io.github.cryschan.berepository.domain.user.exception.UserException;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.cryschan.berepository.domain.user.entity.role.UserRole.USER;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final BlogRepository blogRepository;
    private final BlogTemplateRepository blogTemplateRepository;
    private final UserRepository userRepository;
    private final DashboardRepository dashboardRepository;
    private final ObjectMapper objectMapper;

    public DashboardResponse getDashboardData(Long userId) {
        // Admin 권한 체크
        if (!isAdmin(userId)) {
            throw UserException.accessDenied("대시보드");
        }

        // 오늘 날짜 기준으로 대시보드 데이터 계산
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        DashboardData data = calculateDashboardData(today);

        return new DashboardResponse(
                data.activeUserCount(),
                data.todayBlogCount(),
                data.totalBlogCount(),
                data.categoryDistribution(),
                data.platformUsage(),
                data.todayBlogItemList(),
                data.totalTokenUsage()
        );
    }

    /**
     * 대시보드 데이터를 계산
     *
     * @param date 계산할 날짜 (해당 날짜의 00:00:00)
     * @return 계산된 대시보드 데이터
     */
    private DashboardData calculateDashboardData(LocalDateTime date) {
        // 해당 날짜의 시작과 끝 시간 계산
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59, 999999999);

        // 템플릿을 한 번만 조회하여 재사용 (중복 호출 방지)
        Map<Long, BlogTemplate> templateMap = getTemplateMap();

        // 1) 해당 날짜에 작성된 글 수
        Integer todayBlogCount = blogRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        // 2) 전체 블로그 수
        Integer totalBlogCount = (int) blogRepository.count();

        // 3) 전체 활성 사용자 수
        Integer activeUserCount = userRepository.countByRole(USER);

        // 4) 카테고리 분포 및 플랫폼 분포 계산 (한 번의 순회로 처리)
        Map<String, Long> categoryDistribution = new HashMap<>();
        Map<String, Long> platformUsage = new HashMap<>();
        calculateDistributionAndUsage(categoryDistribution, platformUsage, templateMap);

        // 5) 해당 날짜에 작성된 글 리스트
        List<TodayBlogItem> todayBlogItemList = getTodayBlogList(startOfDay, endOfDay, templateMap);

        // 6) 사용한 토큰 수 (전체 사용자의 토큰 사용량 합계)
        Long totalTokenUsage = getTotalTokenUsage();

        return new DashboardData(
                activeUserCount,
                todayBlogCount,
                totalBlogCount,
                categoryDistribution,
                platformUsage,
                todayBlogItemList,
                totalTokenUsage
        );
    }

    /* 카테고리 분포 및 플랫폼 분포 계산 (한 번의 순회로 처리) */
    private void calculateDistributionAndUsage(
            Map<String, Long> categoryDistribution,
            Map<String, Long> platformUsage,
            Map<Long, BlogTemplate> templateMap
    ) {
        // Blog 테이블에서 모든 블로그 가져오기
        List<Blog> allBlogs = blogRepository.findAll();

        // 카테고리와 플랫폼별로 해당 항목을 사용하는 블로그 수 집계
        Map<String, java.util.Set<Long>> categoryToBlogIds = new HashMap<>();
        Map<String, java.util.Set<Long>> platformToBlogIds = new HashMap<>();

        // 한 번의 순회로 카테고리와 플랫폼 분포를 모두 계산
        for (Blog blog : allBlogs) {
            BlogTemplate template = getTemplateFromBlog(blog, templateMap);
            Long blogId = blog.getId();

            // 템플릿의 각 카테고리에 대해 해당 블로그 ID를 추가
            for (String category : template.getCategories()) {
                categoryToBlogIds.computeIfAbsent(category, k -> new java.util.HashSet<>())
                        .add(blogId);
            }

            // 템플릿의 각 플랫폼에 대해 해당 블로그 ID를 추가
            for (String platform : template.getPlatforms()) {
                platformToBlogIds.computeIfAbsent(platform, k -> new java.util.HashSet<>())
                        .add(blogId);
            }
        }

        // Set의 크기(고유 블로그 수)를 Long으로 변환
        categoryToBlogIds.forEach((category, blogIds) ->
                categoryDistribution.put(category, (long) blogIds.size()));

        platformToBlogIds.forEach((platform, blogIds) ->
                platformUsage.put(platform, (long) blogIds.size()));
    }

    /* 오늘 작성된 글 리스트 생성 */
    private List<TodayBlogItem> getTodayBlogList(
            LocalDateTime start,
            LocalDateTime end,
            Map<Long, BlogTemplate> templateMap
    ) {
        List<Blog> blogs = blogRepository.findAllByCreatedAtBetween(start, end);

        return blogs.stream()
                .map(blog -> {
                    String platform = "Unknown";
                    BlogTemplate template = getTemplateFromBlog(blog, templateMap);
                    if (!template.getPlatforms().isEmpty()) {
                        platform = template.getPlatforms().get(0);
                    }
                    return new TodayBlogItem(
                            blog.getTitle(),
                            platform,
                            blog.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    /* 템플릿을 Map으로 조회하여 캐싱 */
    private Map<Long, BlogTemplate> getTemplateMap() {
        return blogTemplateRepository.findAll().stream()
                .collect(Collectors.toMap(
                        BlogTemplate::getId,
                        template -> template,
                        (existing, replacement) -> existing  // 중복 키 발생 시 기존 값 유지
                ));
    }

    /* Blog에서 템플릿 조회 (공통 로직) */
    private BlogTemplate getTemplateFromBlog(Blog blog, Map<Long, BlogTemplate> templateMap) {
        Long templateId = blog.getBlogTemplateId();
        if (templateId == null) {
            log.error("blogTemplateId is null for blog id: {}. This indicates a data integrity issue.", blog.getId());
            throw new IllegalStateException(
                    String.format("블로그의 템플릿 ID가 null입니다. Blog ID: %d", blog.getId())
            );
        }
        BlogTemplate template = templateMap.get(templateId);
        if (template == null) {
            log.error("BlogTemplate not found for templateId: {} (blog id: {}). This indicates a data integrity issue.", templateId, blog.getId());
            throw new IllegalStateException(
                    String.format("블로그 템플릿을 찾을 수 없습니다. Template ID: %d (Blog ID: %d)", templateId, blog.getId())
            );
        }
        return template;
    }

    /* 사용한 토큰 수 계산 */
    private Long getTotalTokenUsage() {
        return userRepository.findAll().stream()
                .map(user -> user.getTokenUsage() != null ? user.getTokenUsage() : 0L)
                .reduce(0L, Long::sum);
    }

    /**
     * 대시보드 데이터를 계산하여 DB에 저장
     * 스케줄러에서 호출하여 매일 자정에 실행
     *
     * @param date 저장할 날짜 (LocalDateTime - 해당 날짜의 00:00:00)
     * @param adminUserId 관리자 사용자 ID (스케줄러 호출 시 null 가능)
     * @throws RuntimeException 데이터 계산 또는 저장 실패 시
     */
    @Transactional
    public void updateDashboardData(LocalDateTime date, Long adminUserId) {
        log.info("Updating dashboard data for date: {}, adminUserId: {}", date, adminUserId);

        try {
            // 공통 계산 메서드 사용
            DashboardData data = calculateDashboardData(date);
            LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();

            // JSON 직렬화 (실패 시 예외 발생)
            String categoryDistributionJson = serializeToJson(data.categoryDistribution());
            String platformUsageJson = serializeToJson(data.platformUsage());
            String todayBlogListJson = serializeToJson(data.todayBlogItemList());

            // 기존 데이터 조회 (중복 저장 방지)
            Optional<Dashboard> existingDashboard = dashboardRepository.findByDate(startOfDay);

            Dashboard dashboard;
            if (existingDashboard.isPresent()) {
                // 기존 데이터가 있으면 업데이트
                dashboard = existingDashboard.get();
                dashboard.updateData(
                        adminUserId,
                        data.activeUserCount(),
                        data.todayBlogCount(),
                        data.totalBlogCount(),
                        categoryDistributionJson,
                        platformUsageJson,
                        todayBlogListJson
                );
                log.info("Updating existing dashboard data for date: {}", startOfDay);
            } else {
                // 기존 데이터가 없으면 새로 생성
                dashboard = Dashboard.builder()
                        .date(startOfDay)  // 날짜 기준으로 저장 (00:00:00)
                        .adminUserId(adminUserId)  // 관리자 ID 저장
                        .activeUserCount(data.activeUserCount())
                        .todayBlogCount(data.todayBlogCount())
                        .totalBlogCount(data.totalBlogCount())
                        .categoryDistribution(categoryDistributionJson)
                        .platformUsage(platformUsageJson)
                        .todayBlogList(todayBlogListJson)
                        .build();
                log.info("Creating new dashboard data for date: {}", startOfDay);
            }

            dashboardRepository.save(dashboard);
            log.info("Dashboard data saved successfully for date: {}", startOfDay);
        } catch (RuntimeException e) {
            // JSON 직렬화 실패 등 예외 발생 시 로깅 후 재던지기
            log.error("Failed to update dashboard data for date: {}", date, e);
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외 발생 시
            log.error("Unexpected error while updating dashboard data for date: {}", date, e);
            throw new RuntimeException("대시보드 데이터 저장 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 객체를 JSON 문자열로 직렬화
     * 직렬화 실패 시 예외를 던져 호출부에서 처리
     *
     * @param object 직렬화할 객체
     * @return JSON 문자열
     * @throws RuntimeException JSON 직렬화 실패 시
     */
    private String serializeToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON: {}", object, e);
            throw new RuntimeException("대시보드 데이터 직렬화에 실패했습니다.", e);
        }
    }

    /**
     * Admin 권한 체크
     *
     * @param userId 사용자 ID
     * @return Admin 권한 여부
     */
    private boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRole() == UserRole.ADMIN)
                .orElse(false);
    }

    /**
     * 대시보드 데이터를 담는 레코드
     * 공통 계산 메서드의 반환 타입으로 사용
     */
    private record DashboardData(
            Integer activeUserCount,
            Integer todayBlogCount,
            Integer totalBlogCount,
            Map<String, Long> categoryDistribution,
            Map<String, Long> platformUsage,
            List<TodayBlogItem> todayBlogItemList,
            Long totalTokenUsage
    ) {
    }

}
