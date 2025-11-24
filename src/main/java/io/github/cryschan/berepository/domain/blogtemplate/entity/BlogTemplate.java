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
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "blog_template_categories",
            joinColumns = @JoinColumn(name = "blog_template_id")
    )
    @Column(nullable = false)
    @Builder.Default
    private List<String> categories = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "blog_template_platforms",
            joinColumns = @JoinColumn(name = "blog_template_id")
    )
    @Column(nullable = false)
    @Builder.Default
    private List<String> platforms = new ArrayList<>();

    @Column(nullable = false)
    private String shopUrl;

    @Column(nullable = false)
    private boolean includeImages;

    @Column(nullable = false)
    private int imageCount;

    @Column(nullable = false)
    private int charLimit;

    @Column(nullable = false)
    private LocalTime dailyPostTime;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

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
        if (includeImages && (imageCount < 1 || imageCount > 10)) {
            throw new IllegalArgumentException("imageCount must be between 1 and 10 when images are included.");
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
