package io.github.cryschan.berepository.domain.inquiry.repository;

import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.Inquiry;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Inquiry Repository
 *
 * Spring Data JPA의 메서드 네이밍 규칙을 사용하여 자동으로 쿼리 생성
 * - findBy{필드명}OrderBy{필드명}Desc: WHERE절 + ORDER BY절 자동 생성
 */
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    /**
     * 특정 사용자의 모든 문의를 최신순으로 조회
     *
     * 생성되는 쿼리:
     * SELECT * FROM inquiry WHERE user_id = ? ORDER BY created_at DESC
     *
     * 사용처: InquiryService.getMyInquiries()
     */
    List<Inquiry> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 상태별 문의 조회 (최신순)
     *
     * 생성되는 쿼리:
     * SELECT * FROM inquiry WHERE status = ? ORDER BY created_at DESC
     *
     * 사용처:
     * - AdminInquiryService.getPendingInquiries() - PENDING 상태 조회
     * - AdminInquiryService.getPendingInquiriesCount() - PENDING 건수 조회
     */
    List<Inquiry> findByStatusOrderByCreatedAtDesc(InquiryStatus status);
}
