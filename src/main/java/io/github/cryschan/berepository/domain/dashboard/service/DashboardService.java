package io.github.cryschan.berepository.domain.dashboard.service;

import io.github.cryschan.berepository.domain.blog.entity.Blog;
import io.github.cryschan.berepository.domain.blog.repository.BlogRepository;
import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.repository.BlogTemplateRepository;
import io.github.cryschan.berepository.domain.dashboard.dto.DashboardResponse;
import io.github.cryschan.berepository.domain.dashboard.dto.TodayBlogItem;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.cryschan.berepository.domain.user.entity.role.UserRole.USER;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final BlogRepository blogRepository;
    private final BlogTemplateRepository blogTemplateRepository;
    private final UserRepository userRepository;

    public DashboardResponse getDashboardData() {
        // 오늘 날짜
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(LocalTime.from(LocalDateTime.MAX));

        // 1) 오늘 작성된 글 수
        Integer todayBlogCount = blogRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        // 2) 전체 블로그 수
        Integer totalBlogCount = (int) blogRepository.count();

        // 3) 전체 활성 사용자 수 (ex: USER or ADMIN 기준으로 바꿔도 됨)
        Integer activeUserCount = userRepository.countByRole(USER);

        // 4) 카테고리 분포 및 플랫폼 분포 계산 (한 번의 순회로 처리)
        Map<String, Long> categoryDistribution = new HashMap<>();
        Map<String, Long> platformUsage = new HashMap<>();
        calculateDistributionAndUsage(categoryDistribution, platformUsage);

        // 5) 오늘 작성된 글 리스트 (today_blog_list)
        List<TodayBlogItem> todayBlogItemList = getTodayBlogList(startOfDay, endOfDay);

        // 6) 사용한 토큰 수 (전체 사용자의 토큰 사용량 합계)
        Long totalTokenUsage = getTotalTokenUsage();

        return new DashboardResponse(
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
            Map<String, Long> platformUsage
    ) {
        // Blog 테이블에서 모든 블로그 가져오기
        List<Blog> allBlogs = blogRepository.findAll();
        
        // 템플릿을 한 번에 조회하여 Map으로 캐싱
        Map<Long, BlogTemplate> templateMap = getTemplateMap();

        // 카테고리와 플랫폼별로 해당 항목을 사용하는 블로그 수 집계
        Map<String, java.util.Set<Long>> categoryToBlogIds = new HashMap<>();
        Map<String, java.util.Set<Long>> platformToBlogIds = new HashMap<>();
        
        // 한 번의 순회로 카테고리와 플랫폼 분포를 모두 계산
        for (Blog blog : allBlogs) {
            BlogTemplate template = getTemplateFromBlog(blog, templateMap);
            if (template != null) {
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
        }

        // Set의 크기(고유 블로그 수)를 Long으로 변환
        categoryToBlogIds.forEach((category, blogIds) -> 
                categoryDistribution.put(category, (long) blogIds.size()));
        
        platformToBlogIds.forEach((platform, blogIds) -> 
                platformUsage.put(platform, (long) blogIds.size()));
    }

    /* 오늘 작성된 글 리스트 생성 */
    private List<TodayBlogItem> getTodayBlogList(LocalDateTime start, LocalDateTime end) {
        List<Blog> blogs = blogRepository.findAllByCreatedAtBetween(start, end);
        
        // 템플릿을 한 번에 조회하여 Map으로 캐싱 
        Map<Long, BlogTemplate> templateMap = getTemplateMap();

        return blogs.stream()
                .map(blog -> {
                    String platform = "Unknown";
                    BlogTemplate template = getTemplateFromBlog(blog, templateMap);
                    if (template != null && !template.getPlatforms().isEmpty()) {
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
                .collect(Collectors.toMap(BlogTemplate::getId, template -> template));
    }

    /* Blog에서 템플릿 조회 (공통 로직) */
    private BlogTemplate getTemplateFromBlog(Blog blog, Map<Long, BlogTemplate> templateMap) {
        try {
            Long templateId = Long.parseLong(blog.getBlogTemplateId());
            return templateMap.get(templateId);
        } catch (NumberFormatException e) {
            // templateId 파싱 실패 시 null 반환
            return null;
        }
    }

    /* 사용한 토큰 수 계산 */
    private Long getTotalTokenUsage() {
        return userRepository.findAll().stream()
                .map(user -> user.getTokenUsage() != null ? user.getTokenUsage() : 0L)
                .reduce(0L, Long::sum);
    }

}
