package io.github.cryschan.berepository.domain.notice.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

public class NoticeNotFoundException extends DomainException {

    public NoticeNotFoundException(Long id) {
        super(ErrorCode.NOTICE_NOT_FOUND, "공지사항을 찾을 수 없습니다. ID: " + id);
    }

}
