package io.github.cryschan.berepository.domain.user.exception;

import io.github.cryschan.berepository._global.exception.base.ErrorCode;

/**
 * 토큰 관련 예외 클래스
 * JWT 토큰 검증 및 처리 중 발생하는 예외를 처리합니다.
 */
public class TokenException extends RuntimeException {

    private final ErrorCode errorCode;

    public TokenException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // ==================== 정적 팩토리 메서드 ====================

    /**
     * 토큰이 유효하지 않을 때 발생하는 예외
     */
    public static TokenException invalid() {
        return new TokenException(ErrorCode.INVALID_TOKEN);
    }

    /**
     * 토큰이 만료되었을 때 발생하는 예외
     */
    public static TokenException expired() {
        return new TokenException(ErrorCode.EXPIRED_TOKEN);
    }

    /**
     * 토큰을 찾을 수 없을 때 발생하는 예외
     */
    public static TokenException notFound() {
        return new TokenException(ErrorCode.TOKEN_NOT_FOUND);
    }

    /**
     * 예외의 에러 코드를 반환합니다.
     *
     * @return 에러 코드
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
