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

    private Long id;

    private Long adminUserId;

    private String answerContent;

    private LocalDateTime answeredAt;
}
