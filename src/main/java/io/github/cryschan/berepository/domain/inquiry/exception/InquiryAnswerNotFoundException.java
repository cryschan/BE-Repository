package io.github.cryschan.berepository.domain.inquiry.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

/**
 * 문의에 대한 답변을 찾을 수 없을 때 발생하는 예외
 */
public class InquiryAnswerNotFoundException extends DomainException {

    public InquiryAnswerNotFoundException(Long inquiryId) {
        super(ErrorCode.INQUIRY_ANSWER_NOT_FOUND,
                String.format("문의(ID: %d)에 대한 답변을 찾을 수 없습니다.", inquiryId));
    }

}
