package io.github.cryschan.berepository.domain.faqs.repository;

import io.github.cryschan.berepository.domain.faqs.entity.Faqs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/*
FAQ 목록을 sort_order 기준 오름차순으로 조회합니다.
 */
public interface FaqsRepository extends JpaRepository<Faqs, Long> {

    List<Faqs> findAllByOrderBySortOrderAsc();
}
