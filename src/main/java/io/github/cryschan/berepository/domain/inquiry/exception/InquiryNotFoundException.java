package io.github.cryschan.berepository.domain.inquiry.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

/**
 * 문의를 찾을 수 없을 때 발생하는 예외
 *
 * 발생 시점:
 * - 존재하지 않는 문의 ID로 조회 시도
 * - 삭제된 문의 접근 시도
 */
public class InquiryNotFoundException extends DomainException {
    public InquiryNotFoundException(Long inquiryId) {
        super(ErrorCode.INQUIRY_NOT_FOUND, "문의를 찾을 수 없습니다. ID: " + inquiryId);
    }
}
