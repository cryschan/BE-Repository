package io.github.cryschan.berepository.domain.inquiry.dto.Request;

import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 문의 생성 요청 DTO
 *
 * 플로우:
 * 1. 사용자가 문의 작성 폼 제출
 * 2. Controller에서 이 DTO로 데이터 받음
 * 3. Service에서 Inquiry 엔티티로 변환 후 저장
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateInquiryRequest {

    private String title;

    private InquiryCategory inquiryCategory;

    private String content;
}
