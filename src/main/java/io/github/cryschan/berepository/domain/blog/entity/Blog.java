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

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "blog_template_id", nullable = false)
    private String blogTemplateId;

    @Column(nullable = false)
    private String title;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Builder
    public Blog(String blogTemplateId, String title, String imgUrl, String content, String userId) {
        this.blogTemplateId = blogTemplateId;
        this.title = title;
        this.imgUrl = imgUrl;
        this.content = content;
        this.userId = userId;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void updateBlogTemplate(String blogTemplateId) {
        this.blogTemplateId = blogTemplateId;
    }

}