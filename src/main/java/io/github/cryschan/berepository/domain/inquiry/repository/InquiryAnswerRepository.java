package io.github.cryschan.berepository.domain.inquiry.repository;

import io.github.cryschan.berepository.domain.inquiry.entity.Answer.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {

    /**
     * 특정 문의에 대한 답변 조회
     * 사용: 문의 상세 조회 시 답변 포함
     */
    Optional<InquiryAnswer> findByInquiryId(Long inquiryId);

    /**
     * 특정 문의에 답변이 존재하는지 확인
     * 사용: 중복 답변 방지
     */
    boolean existsByInquiryId(Long inquiryId);
}
