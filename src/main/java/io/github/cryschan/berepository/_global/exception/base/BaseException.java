package io.github.cryschan.berepository._global.exception.base;

import org.springframework.http.HttpStatus;

/**
 * 모든 비즈니스 예외의 기본 클래스
 * 이 클래스를 상속받은 예외들은 GlobalExceptionHandler에서 일괄 처리됩니다.
 */
public abstract class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * ErrorCode만으로 예외 생성
     * ErrorCode에 정의된 기본 메시지를 사용합니다.
     */
    protected BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode와 커스텀 메시지로 예외 생성
     * 더 구체적인 메시지가 필요한 경우 사용합니다.
     */
    protected BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode와 원인 예외로 예외 생성
     * 다른 예외를 래핑할 때 사용합니다.
     */
    protected BaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode, 커스텀 메시지, 원인 예외로 예외 생성
     * 가장 상세한 정보를 포함하는 생성자입니다.
     */
    protected BaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }

    public int getHttpStatusCode() {
        return errorCode.getHttpStatusCode();
    }
}