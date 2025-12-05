package io.github.cryschan.berepository.domain.inquiry.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

/**
 * 페이지 파라미터가 유효하지 않을 때 발생하는 예외
 */
public class InvalidInquiryPageException extends DomainException {

    public InvalidInquiryPageException(int page) {
        super(ErrorCode.INQUIRY_INVALID_PAGE,
                String.format("페이지 번호는 1 이상이어야 합니다. 입력값: %d", page));
    }
}
