package io.github.cryschan.berepository.domain.notice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "notices")
@EntityListeners(AuditingEntityListener.class)
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private boolean isImportant;        // 중요 공지 여부

    @Builder
    public Notice(String title, String content, boolean isImportant) {
        this.title = title;
        this.content = content;
        this.isImportant = isImportant;
    }

    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void markAsImportant() {
        this.isImportant = true;
    }

    public void unmarkAsImportant() {
        this.isImportant = false;
    }

}
