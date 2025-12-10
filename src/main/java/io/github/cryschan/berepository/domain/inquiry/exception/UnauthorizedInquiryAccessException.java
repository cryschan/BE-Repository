package io.github.cryschan.berepository.domain.inquiry.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

/**
 * 문의에 대한 권한이 없을 때 발생하는 예외
 *
 * 발생 시점:
 * - 다른 사용자의 문의를 조회/수정/삭제하려 할 때
 * - 본인의 문의가 아닌데 접근하려 할 때
 */
public class UnauthorizedInquiryAccessException extends DomainException {
    public UnauthorizedInquiryAccessException() {
        super(ErrorCode.INQUIRY_ACCESS_DENIED);
    }
}
