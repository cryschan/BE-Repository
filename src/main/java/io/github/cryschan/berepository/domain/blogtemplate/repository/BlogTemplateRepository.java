package io.github.cryschan.berepository.domain.blogtemplate.repository;

import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BlogTemplateRepository extends JpaRepository<BlogTemplate, Long> {

    @Query("SELECT DISTINCT bt FROM BlogTemplate bt JOIN bt.categories category WHERE category IN :categories")
    List<BlogTemplate> findByAnyCategory(@Param("categories") Collection<String> categories);

    @Query("SELECT DISTINCT bt FROM BlogTemplate bt JOIN bt.platforms platform WHERE platform IN :platforms")
    List<BlogTemplate> findByAnyPlatform(@Param("platforms") Collection<String> platforms);

    @Query("SELECT DISTINCT bt FROM BlogTemplate bt " +
            "LEFT JOIN FETCH bt.categories " +
            "WHERE bt.dailyPostTime = :postTime")
    List<BlogTemplate> findByDailyPostTimeWithCollections(@Param("postTime") LocalTime postTime);

    Optional<BlogTemplate> findByUserId(Long userId);
}
