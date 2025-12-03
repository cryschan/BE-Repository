package io.github.cryschan.berepository.domain.inquiry.dto.Response;

import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryCategory;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 문의 목록 응답 DTO
 *
 * 플로우:
 * 1. 사용자가 "내 문의 목록" 페이지 접속
 * 2. Service에서 해당 사용자의 모든 문의 조회
 * 3. 각 Inquiry를 이 DTO로 변환하여 리스트로 반환
 *
 * 참고: 목록에서는 전체 내용 대신 요약 정보만 제공
 */
@Getter
@Builder
@AllArgsConstructor
public class InquiryListResponse {

    // TODO: 문의 ID (상세 페이지 이동용)
    private Long id;

    // TODO: 문의 제목
    private String title;

    // TODO: 문의 카테고리
    private InquiryCategory inquiryCategory;

    // TODO: 문의 상태 (답변 여부 확인용)
    private InquiryStatus status;

    // TODO: 문의 생성일
    private LocalDateTime createdAt;

    // TODO: 답변 여부 (빠른 확인용)
    private boolean hasAnswer;
}
