package io.github.cryschan.berepository.domain.inquiry.repository;

import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.Inquiry;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // 사용자 문의 파트 - 필요한 메서드들

    /**
     * 특정 사용자의 모든 문의를 최신순으로 조회
     * 사용: 내 문의 목록 조회
     */
    List<Inquiry> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 관리자용 - 상태별 문의 조회 (최신순)
     * 사용: 답변 대기 중인 문의 목록 조회
     */
    List<Inquiry> findByStatusOrderByCreatedAtDesc(InquiryStatus status);

    /**
     * 관리자용 - 문의 목록 조회
     * 사용: 관리자인 경우 조회 허용
     */
    @Query("SELECT i FROM Inquiry i JOIN FETCH i.user WHERE i.userId = :userId")
    List<Inquiry> findByUserIdWithUser(@Param("user_id") Long userId);
}
