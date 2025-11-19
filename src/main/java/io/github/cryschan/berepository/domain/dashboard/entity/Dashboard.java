package io.github.cryschan.berepository.domain.dashboard.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dashboards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "admin_user_id", nullable = false)
    private String adminUserId;

    @Column(name = "active_user_count", nullable = false)
    private Integer activeUserCount;

    @Column(name = "today_blog_count", nullable = false)
    private Integer todayBlogCount;

    @Column(name = "total_blog_count", nullable = false)
    private Integer totalBlogCount;

    @Column(name = "category_distribution", columnDefinition = "TEXT")
    private String categoryDistribution;

    @Column(name = "platform_usage", columnDefinition = "TEXT")
    private String platformUsage;

    @Column(name = "today_blog_list", columnDefinition = "TEXT")
    private String todayBlogList;

    @Builder
    public Dashboard(String adminUserId, Integer activeUserCount, Integer todayBlogCount, Integer totalBlogCount, String categoryDistribution, String platformUsage, String todayBlogList) {
        this.adminUserId = adminUserId;
        this.activeUserCount = activeUserCount;
        this.todayBlogCount = todayBlogCount;
        this.totalBlogCount = totalBlogCount;
        this.categoryDistribution = categoryDistribution;
        this.platformUsage = platformUsage;
        this.todayBlogList = todayBlogList;
    }

    public void updateStatistics(Integer activeUserCount, Integer todayBlogCount, Integer totalBlogCount) {
        this.activeUserCount = activeUserCount;
        this.todayBlogCount = todayBlogCount;
        this.totalBlogCount = totalBlogCount;
    }

    public void updateCategoryDistribution(String categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }

    public void updatePlatformUsage(String platformUsage) {
        this.platformUsage = platformUsage;
    }

    public void updateTodayBlogList(String todayBlogList) {
        this.todayBlogList = todayBlogList;
    }
}