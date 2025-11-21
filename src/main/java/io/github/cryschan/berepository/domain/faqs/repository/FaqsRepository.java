package io.github.cryschan.berepository.domain.faqs.repository;

import io.github.cryschan.berepository.domain.faqs.entity.Faqs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/*
FAQ 목록을 created_at 기준으로 조회합니다.
 */
public interface FaqsRepository extends JpaRepository<Faqs, Long> {


    List<Faqs> findAllByOrderByCreatedAtDesc();
}
