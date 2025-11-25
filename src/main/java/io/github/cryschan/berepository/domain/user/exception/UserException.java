package io.github.cryschan.berepository.domain.user.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

/**
 * User 도메인 통합 예외 클래스
 * User 도메인에서 발생하는 모든 예외를 정적 팩토리 메서드로 제공합니다.
 */
public class UserException extends DomainException {

    protected UserException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected UserException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    protected UserException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    protected UserException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    // ==================== 정적 팩토리 메서드 ====================

    /**
     * 사용자를 찾을 수 없을 때 발생하는 예외
     */
    public static UserException notFound(Long userId) {
        return new UserException(
                ErrorCode.USER_NOT_FOUND,
                String.format("사용자를 찾을 수 없습니다. ID: %d", userId)
        );
    }

    /**
     * 이메일로 사용자를 찾을 수 없을 때 발생하는 예외
     */
    public static UserException notFound(String email) {
        return new UserException(
                ErrorCode.USER_NOT_FOUND,
                String.format("사용자를 찾을 수 없습니다. Email: %s", email)
        );
    }

    /**
     * 일반적인 사용자 찾기 실패 예외
     */
    public static UserException notFound() {
        return new UserException(ErrorCode.USER_NOT_FOUND);
    }

    /**
     * 이메일 중복 시 발생하는 예외
     */
    public static UserException duplication(String email) {
        return new UserException(
                ErrorCode.USER_DUPLICATION,
                String.format("이미 존재하는 이메일입니다: %s", email)
        );
    }

    /**
     * 로그인 실패 시 발생하는 예외 (잘못된 인증 정보)
     */
    public static UserException invalidCredentials() {
        return new UserException(ErrorCode.INVALID_CREDENTIALS);
    }

    /**
     * 로그인 실패 시 발생하는 예외 (커스텀 메시지)
     */
    public static UserException invalidCredentials(String message) {
        return new UserException(ErrorCode.INVALID_CREDENTIALS, message);
    }

    /**
     * 인증되지 않은 접근 시 발생하는 예외
     */
    public static UserException unauthorized() {
        return new UserException(ErrorCode.UNAUTHORIZED);
    }

    /**
     * 인증되지 않은 접근 시 발생하는 예외 (커스텀 메시지)
     */
    public static UserException unauthorized(String message) {
        return new UserException(ErrorCode.UNAUTHORIZED, message);
    }

    /**
     * 접근 권한이 없을 때 발생하는 예외
     */
    public static UserException accessDenied() {
        return new UserException(ErrorCode.ACCESS_DENIED);
    }

    /**
     * 접근 권한이 없을 때 발생하는 예외 (리소스 명시)
     */
    public static UserException accessDenied(String resource) {
        return new UserException(
                ErrorCode.ACCESS_DENIED,
                String.format("%s에 대한 접근 권한이 없습니다", resource)
        );
    }

    /**
     * 입력값이 잘못되었을 때 발생하는 예외 (ex: null 데이터 조회)
     */
    public static UserException invalidInput(String message) {
        return new UserException(ErrorCode.INVALID_INPUT, message);
    }
}