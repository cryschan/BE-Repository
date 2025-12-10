package io.github.cryschan.berepository.domain.inquiry.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

/**
 * 이미 답변된 문의에 대해 수정 시도 시 발생하는 예외
 *
 * 발생 시점:
 * - 답변이 달린 문의를 수정하려 할 때
 * - 답변이 달린 문의를 삭제하려 할 때 (정책에 따라)
 */
public class InquiryAlreadyAnsweredException extends DomainException {
    public InquiryAlreadyAnsweredException() {
        super(ErrorCode.INQUIRY_ALREADY_ANSWERED);
    }
}
