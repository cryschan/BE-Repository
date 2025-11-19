package io.github.cryschan.berepository.domain.keyword.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "blog_id", nullable = false)
    private String blogId;

    @Column(nullable = false)
    private String keyword;

    @Builder
    public Keyword(String blogId, String keyword) {
        this.blogId = blogId;
        this.keyword = keyword;
    }

    public void updateKeyword(String keyword) {
        this.keyword = keyword;
    }
}