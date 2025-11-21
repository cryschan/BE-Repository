package io.github.cryschan.berepository.domain.faqs.repository;

import io.github.cryschan.berepository.domain.faqs.entity.Faqs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqsRepository extends JpaRepository<Faqs, String> {

    List<Faqs> findAllByOrderBySortOrderAscCreatedAtAsc();
}
