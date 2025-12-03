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

    // TODO: 문의 제목 (필수, 최대 200자)
    private String title;

    // TODO: 문의 카테고리 (필수) - FEATURE, PAYMENT, ACCOUNT, ETC
    private InquiryCategory inquiryCategory;

    // TODO: 문의 내용 (필수)
    private String content;
}
