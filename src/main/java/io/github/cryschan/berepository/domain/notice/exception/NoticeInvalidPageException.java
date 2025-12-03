package io.github.cryschan.berepository.domain.notice.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

public class NoticeInvalidPageException extends DomainException {

    public NoticeInvalidPageException(int page, int totalPages) {
        super(ErrorCode.NOTICE_INVALID_PAGE, 
                String.format("페이지 %d는 존재하지 않습니다. 전체 페이지 수: %d", page, totalPages));
    }

    public NoticeInvalidPageException(String message) {
        super(ErrorCode.NOTICE_INVALID_PAGE, message);
    }
}

