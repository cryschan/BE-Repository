package io.github.cryschan.berepository.domain.inquiry.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 답변 작성 요청 DTO
 *
 * 플로우:
 * 1. 관리자가 문의 상세 모달에서 답변 작성
 * 2. Controller에서 이 DTO로 데이터 받음
 * 3. Service에서 InquiryAnswer 엔티티 생성 후 저장
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnswerRequest {

    // 답변 내용
    private String answerContent;
}
