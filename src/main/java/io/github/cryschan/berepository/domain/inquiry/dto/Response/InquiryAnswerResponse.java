package io.github.cryschan.berepository.domain.inquiry.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 답변 정보 응답 DTO
 *
 * 플로우:
 * 1. InquiryDetailResponse에 포함되어 반환
 * 2. 답변이 있는 경우에만 생성됨
 */
@Getter
@Builder
@AllArgsConstructor
public class InquiryAnswerResponse {

    // TODO: 답변 ID
    private Long id;

    // TODO: 답변 작성자 (관리자) ID
    private Long adminUserId;

    // TODO: 답변 내용
    private String answerContent;

    // TODO: 답변 작성일
    private LocalDateTime createdAt;
}
