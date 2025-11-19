package io.github.cryschan.berepository.domain.template.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blog_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class BlogTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ElementCollection
    @CollectionTable(name = "blog_template_categories",
            joinColumns = @JoinColumn(name = "blog_template_id"))
    @Column(name = "category")
    private List<String> categories = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "blog_template_platforms",
            joinColumns = @JoinColumn(name = "blog_template_id"))
    @Column(name = "platform")
    private List<String> platforms = new ArrayList<>();

    @Column(name = "shop_url")
    private String shopUrl;

    @Column(name = "include_images", nullable = false)
    private Boolean includeImages;

    @Column(name = "image_count")
    private Integer imageCount;

    @Column(name = "char_limit")
    private Integer charLimit;

    @Column(name = "daily_post_time")
    private String dailyPostTime;

    @Builder
    public BlogTemplate(List<String> categories, List<String> platforms,
                        String shopUrl, Boolean includeImages, Integer imageCount,
                        Integer charLimit, String dailyPostTime) {
        this.categories = categories != null ? categories : new ArrayList<>();
        this.platforms = platforms != null ? platforms : new ArrayList<>();
        this.shopUrl = shopUrl;
        this.includeImages = includeImages;
        this.imageCount = imageCount;
        this.charLimit = charLimit;
        this.dailyPostTime = dailyPostTime;
    }

    public void updateCategories(List<String> categories) {
        this.categories = categories;
    }

    public void updatePlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    public void updateShopUrl(String shopUrl) {
        this.shopUrl = shopUrl;
    }

    public void updateIncludeImages(Boolean includeImages) {
        this.includeImages = includeImages;
    }

    public void updateImageCount(Integer imageCount) {
        this.imageCount = imageCount;
    }

    public void updateCharLimit(Integer charLimit) {
        this.charLimit = charLimit;
    }

    public void updateDailyPostTime(String dailyPostTime) {
        this.dailyPostTime = dailyPostTime;
    }
}