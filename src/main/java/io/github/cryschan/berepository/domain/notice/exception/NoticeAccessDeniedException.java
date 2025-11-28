package io.github.cryschan.berepository.domain.notice.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

public class NoticeAccessDeniedException extends DomainException {

    public NoticeAccessDeniedException() {
        super(ErrorCode.NOTICE_ACCESS_DENIED);
    }

    public NoticeAccessDeniedException(String message) {
        super(ErrorCode.NOTICE_ACCESS_DENIED, message);
    }
}

