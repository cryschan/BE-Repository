package io.github.cryschan.berepository.domain.blog.repository;

import io.github.cryschan.berepository.domain.blog.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BlogRepository extends JpaRepository<Blog, Long> {

    // 특정 유저의 블로그 목록 조회 (최신순)
    Page<Blog> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 기간별 블로그 개수
    Integer countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 기간별 블로그 목록
    List<Blog> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}