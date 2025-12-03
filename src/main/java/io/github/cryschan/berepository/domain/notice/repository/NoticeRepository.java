package io.github.cryschan.berepository.domain.notice.repository;

import io.github.cryschan.berepository.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
