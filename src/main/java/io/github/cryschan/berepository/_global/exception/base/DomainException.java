package io.github.cryschan.berepository._global.exception.base;

/**
 * 도메인별 예외의 기본 클래스
 * 각 도메인(User, Board, FAQ 등)의 예외들은 이 클래스를 상속받습니다.
 */
public abstract class DomainException extends BaseException {

    protected DomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected DomainException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    protected DomainException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    protected DomainException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}