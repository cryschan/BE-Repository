package io.github.cryschan.berepository.domain.dashboard.repository;

import io.github.cryschan.berepository.domain.dashboard.entity.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, Long> {

    /**
     * 날짜 기준으로 대시보드 데이터 조회 (중복 저장 방지용)
     *
     * @param date 조회할 날짜 (시간은 무시하고 날짜만 비교)
     * @return 해당 날짜의 대시보드 데이터
     */
    @Query("SELECT d FROM Dashboard d WHERE DATE(d.date) = DATE(:date)")
    Optional<Dashboard> findByDate(@Param("date") LocalDateTime date);
}
