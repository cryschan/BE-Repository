package io.github.cryschan.berepository.domain.inquiry.exception;

/**
 * 이미 답변된 문의에 대해 수정 시도 시 발생하는 예외
 *
 * 발생 시점:
 * - 답변이 달린 문의를 수정하려 할 때
 * - 답변이 달린 문의를 삭제하려 할 때 (정책에 따라)
 */
public class InquiryAlreadyAnsweredException extends RuntimeException {
    public InquiryAlreadyAnsweredException() {
        super("이미 답변된 문의는 수정할 수 없습니다.");
    }
}
