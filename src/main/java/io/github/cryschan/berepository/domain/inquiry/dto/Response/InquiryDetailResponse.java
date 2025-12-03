package io.github.cryschan.berepository.domain.inquiry.dto.Response;

import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryCategory;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 문의 상세 응답 DTO
 *
 * 플로우:
 * 1. 사용자가 특정 문의 클릭
 * 2. Service에서 Inquiry + InquiryAnswer 조회
 * 3. 이 DTO로 변환하여 반환
 * 4. 답변이 있으면 answer 필드에 포함, 없으면 null
 */
@Getter
@Builder
@AllArgsConstructor
public class InquiryDetailResponse {

    // TODO: 문의 ID
    private Long id;

    // TODO: 작성자 ID (본인 확인용)
    private Long userId;

    // TODO: 문의 제목
    private String title;

    // TODO: 문의 카테고리
    private InquiryCategory inquiryCategory;

    // TODO: 문의 내용
    private String content;

    // TODO: 문의 상태 (PENDING, IN_PROGRESS, COMPLETED)
    private InquiryStatus status;

    // TODO: 문의 생성일
    private LocalDateTime createdAt;

    // TODO: 문의 수정일
    private LocalDateTime updatedAt;

    // TODO: 답변 정보 (답변이 없으면 null)
    private InquiryAnswerResponse answer;
}
