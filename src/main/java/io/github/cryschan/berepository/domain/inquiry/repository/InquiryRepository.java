package io.github.cryschan.berepository.domain.inquiry.repository;

import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.Inquiry;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * 특정 사용자의 모든 문의를 최신순으로 조회 (페이징)
     *
     * 생성되는 쿼리:
     * SELECT * FROM inquiry WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?
     *
     * 사용처: InquiryService.getMyInquiries() - 페이징 지원
     */
    Page<Inquiry> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

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

    /**
     * 상태별 문의 조회 (페이징)
     *
     * 생성되는 쿼리:
     * SELECT * FROM inquiry WHERE status = ? ORDER BY created_at DESC LIMIT ? OFFSET ?
     *
     * 사용처:
     * - AdminInquiryService.getAllInquiries() - 상태별 필터링된 문의 목록 (페이징)
     */
    Page<Inquiry> findByStatus(InquiryStatus status, Pageable pageable);

    /**
     * 상태별 문의 건수를 반환
     */
    long countByStatus(InquiryStatus status);
}
