package io.github.cryschan.berepository.domain.dashboard.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Dashboard {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String adminUserId;         // 관리자(사용자) id

    private Integer activeUserCount;    // 활성 사용자 수

    private Integer todayBlogCount;     // 금일 생성된 게시글 수

    private Integer totalBlogCount;     // 총 게시글 수

    private String categoryDistribution;    // 카테고리 분포 정보

    @Column(columnDefinition = "TEXT")
    private String platformUsage;       // 플랫폼별 사용 통계

    @Column(columnDefinition = "TEXT")
    private String todayBlogList;       // 금일 게시글 목록

    // 대시보드 기준 날짜 (날짜별 중복 저장 방지용)
    private LocalDateTime date;

    @Builder
    public Dashboard(String adminUserId, Integer activeUserCount, Integer todayBlogCount, Integer totalBlogCount, String categoryDistribution, String platformUsage, String todayBlogList, LocalDateTime date) {
        this.adminUserId = adminUserId;
        this.activeUserCount = activeUserCount;
        this.todayBlogCount = todayBlogCount;
        this.totalBlogCount = totalBlogCount;
        this.categoryDistribution = categoryDistribution;
        this.platformUsage = platformUsage;
        this.todayBlogList = todayBlogList;
        this.date = date;
    }

    /**
     * 대시보드 데이터를 업데이트합니다.
     * 중복 저장 방지를 위해 기존 엔티티를 업데이트할 때 사용합니다.
     */
    public void updateData(
            Integer activeUserCount,
            Integer todayBlogCount,
            Integer totalBlogCount,
            String categoryDistribution,
            String platformUsage,
            String todayBlogList
    ) {
        this.activeUserCount = activeUserCount;
        this.todayBlogCount = todayBlogCount;
        this.totalBlogCount = totalBlogCount;
        this.categoryDistribution = categoryDistribution;
        this.platformUsage = platformUsage;
        this.todayBlogList = todayBlogList;
    }

}
