package io.github.cryschan.berepository.domain.blog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "blogs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Blog {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "blog_template_id", nullable = false)
    private Long blogTemplateId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String category;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder
        private Blog(Long blogTemplateId, String title,
                 String content, String category, Long userId) {
        this.blogTemplateId = blogTemplateId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.userId = userId;
    }

    /**
     * 스케줄러에서 블로그 생성 시 사용하는 정적 팩토리 메서드
     */
    public static Blog create(Long blogTemplateId, String title,
                              String content, String category, Long userId) {
        return Blog.builder()
                .blogTemplateId(blogTemplateId)
                .title(title)
                .content(content)
                .category(category)
                .userId(userId)
                .build();
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateBlogTemplate(Long blogTemplateId) {
        this.blogTemplateId = blogTemplateId;
    }

    public void updateCategory(String category) {
        this.category = category;
    }

    public void update(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }
}