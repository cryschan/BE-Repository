package io.github.cryschan.berepository.domain.user.exception;

import io.github.cryschan.berepository._global.exception.base.ErrorCode;

/**
 * 인증되지 않은 사용자가 접근을 시도할 때 발생하는 예외입니다.
 * <p>
 * 로그인하지 않은 사용자나 유효하지 않은 토큰으로 인증이 필요한 리소스에 접근할 때 발생합니다.
 * HTTP 401 (Unauthorized) 응답으로 변환됩니다.
 * </p>
 *
 * @author cryschan
 * @since 1.0
 */
public class UnauthorizedException extends UserException {

    /**
     * 기본 예외 메시지와 함께 예외를 생성합니다.
     */
    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED, "인증되지 않은 사용자입니다.");
    }

    /**
     * 사용자 정의 메시지와 함께 예외를 생성합니다.
     *
     * @param message 예외 메시지
     */
    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

    /**
     * 사용자 ID 정보와 함께 예외를 생성합니다.
     *
     * @param userId 인증 실패한 사용자 ID
     */
    public UnauthorizedException(Long userId) {
        super(ErrorCode.UNAUTHORIZED, "인증되지 않은 사용자입니다. ID: " + userId);
    }
}