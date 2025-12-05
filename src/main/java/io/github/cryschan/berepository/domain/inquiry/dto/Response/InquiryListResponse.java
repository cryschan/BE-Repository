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

    private Long id;

    private String title;

    private InquiryCategory inquiryCategory;

    private InquiryStatus status;

    private LocalDateTime createdAt;

    private boolean hasAnswer;
}
