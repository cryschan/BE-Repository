package io.github.cryschan.berepository.domain.inquiry.dto.Response;

import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryCategory;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자용 문의 목록 응답 DTO
 *
 * 사용자 정보를 포함한 관리자 전용 응답
 */
@Getter
@Builder
@AllArgsConstructor
public class AdminInquiryListResponse {

    private Long id;
    private Long userId;
    private String userEmail;          // 사용자 이메일 (kim@example.com)
    private String userName;           // 사용자 이름 (김민수)
    private String title;
    private InquiryCategory inquiryCategory;
    private InquiryStatus status;
    private LocalDateTime createdAt;
    private boolean hasAnswer;
}
