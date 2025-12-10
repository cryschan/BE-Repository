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

    private Long id;

    private Long userId;

    private String title;

    private InquiryCategory inquiryCategory;

    private String content;

    private InquiryStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private InquiryAnswerResponse answer;
}
