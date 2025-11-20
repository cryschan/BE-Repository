package io.github.cryschan.berepository.domain.blogtemplate.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "blog_templates")
public class BlogTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_template_id", updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "blog_template_categories",
            joinColumns = @JoinColumn(name = "blog_template_id")
    )
    @Column(name = "category", nullable = false)
    @Builder.Default
    private List<String> categories = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "blog_template_platforms",
            joinColumns = @JoinColumn(name = "blog_template_id")
    )
    @Column(name = "platform", nullable = false)
    @Builder.Default
    private List<String> platforms = new ArrayList<>();

    @Column(name = "shop_url", nullable = false)
    private String shopUrl;

    @Column(name = "include_images", nullable = false)
    private boolean includeImages;

    @Column(name = "image_count", nullable = false)
    private int imageCount;

    @Column(name = "char_limit", nullable = false)
    private int charLimit;

    @Column(name = "daily_post_time", nullable = false)
    private LocalTime dailyPostTime;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (!includeImages) {
            imageCount = 0;
        }
    }

    public void updateImageOptions(boolean includeImages, int imageCount) {
        if (includeImages && imageCount < 1) {
            throw new IllegalArgumentException("imageCount must be positive when images are included.");
        }
        this.includeImages = includeImages;
        this.imageCount = includeImages ? imageCount : 0;
    }

    public void updateTemplate(
            String title,
            List<String> categories,
            List<String> platforms,
            String shopUrl,
            boolean includeImages,
            int imageCount,
            int charLimit,
            LocalTime dailyPostTime
    ) {
        this.title = title;
        this.categories.clear();
        this.categories.addAll(categories);
        this.platforms.clear();
        this.platforms.addAll(platforms);
        this.shopUrl = shopUrl;
        updateImageOptions(includeImages, imageCount);
        this.charLimit = charLimit;
        this.dailyPostTime = dailyPostTime;
    }
}
