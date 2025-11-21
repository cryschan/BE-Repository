package io.github.cryschan.berepository.domain.blog.repository;

import io.github.cryschan.berepository.domain.blog.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    Integer countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Blog> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}
