package io.github.cryschan.berepository.domain.user.exception;

import io.github.cryschan.berepository._global.exception.base.ErrorCode;

/**
 * 로그인 실패 시 발생하는 예외입니다.
 * <p>
 * 이메일 또는 비밀번호가 올바르지 않을 때 발생합니다.
 * HTTP 401 (Unauthorized) 응답으로 변환됩니다.
 * </p>
 *
 * @author cryschan
 * @since 1.0
 */
public class InvalidCredentialsException extends UserException {

    /**
     * 이메일 정보와 함께 예외를 생성합니다.
     *
     * @param email 로그인 시도한 이메일
     */
    public InvalidCredentialsException(String email) {
        super(ErrorCode.INVALID_CREDENTIALS, "이메일 또는 비밀번호가 올바르지 않습니다: " + email);
    }

    /**
     * 기본 예외 메시지와 함께 예외를 생성합니다.
     */
    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS, "이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}