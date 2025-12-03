package io.github.cryschan.berepository.domain.inquiry.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 문의 수정 요청 DTO
 *
 * 플로우:
 * 1. 사용자가 자신의 문의 수정
 * 2. Controller에서 이 DTO로 데이터 받음
 * 3. Service에서 Inquiry 엔티티 조회 후 updateContent() 메서드 호출
 *
 * 참고: 답변이 달린 문의는 수정 불가 (Service에서 검증)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInquiryRequest {

    // TODO: 수정할 제목
    private String title;

    // TODO: 수정할 내용
    private String content;

}
