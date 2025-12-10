package io.github.cryschan.berepository.domain.inquiry.repository;

import io.github.cryschan.berepository.domain.inquiry.entity.Answer.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * InquiryAnswer Repository
 *
 * Spring Data JPA의 메서드 네이밍 규칙 사용
 * - findBy{필드명}: WHERE절 자동 생성
 * - existsBy{필드명}: COUNT(*) > 0 쿼리 자동 생성
 */
public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {

    /**
     * 특정 문의에 대한 답변 조회
     *
     * 생성되는 쿼리:
     * SELECT * FROM inquiry_answer WHERE inquiry_id = ? LIMIT 1
     *
     * 사용처:
     * - InquiryService.convertToDetailResponse() - 문의 상세 조회 시 답변 포함
     * - AdminInquiryService.convertToAdminListResponse() - 답변 존재 여부 확인
     *
     * @param inquiryId 문의 ID
     * @return 답변이 있으면 Optional.of(answer), 없으면 Optional.empty()
     */
    Optional<InquiryAnswer> findByInquiry_Id(Long inquiryId);

    /**
     * 특정 문의에 답변이 존재하는지 확인 (중복 답변 방지용)
     *
     * 생성되는 쿼리:
     * SELECT COUNT(*) > 0 FROM inquiry_answer WHERE inquiry_id = ?
     *
     * 사용처:
     * - AdminInquiryService.answerInquiry() - 중복 답변 방지
     *
     * @param inquiryId 문의 ID
     * @return 답변이 존재하면 true, 없으면 false
     */
    boolean existsByInquiry_Id(Long inquiryId);

    /**
     * 주어진 문의 목록 중 답변이 존재하는 문의 ID 목록을 반환
     */
    @Query("SELECT ia.inquiry.id FROM InquiryAnswer ia WHERE ia.inquiry.id IN :inquiryIds")
    List<Long> findAnsweredInquiryIds(@Param("inquiryIds") List<Long> inquiryIds);
}
