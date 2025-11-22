package io.github.cryschan.berepository.domain.user.exception;

import io.github.cryschan.berepository._global.exception.base.ErrorCode;

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외입니다.
 * <p>
 * 존재하지 않는 이메일이나 사용자 ID로 조회를 시도할 때 발생합니다.
 * HTTP 404 (Not Found) 응답으로 변환됩니다.
 * </p>
 *
 * @author cryschan
 * @since 1.0
 */
public class UserNotFoundException extends UserException {

    /**
     * 이메일 정보와 함께 예외를 생성합니다.
     *
     * @param email 찾을 수 없는 사용자의 이메일
     */
    public UserNotFoundException(String email) {
        super(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + email);
    }

    /**
     * 사용자 ID와 함께 예외를 생성합니다.
     *
     * @param userId 찾을 수 없는 사용자의 ID
     */
    public UserNotFoundException(Long userId) {
        super(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다. ID: " + userId);
    }

    /**
     * 기본 예외 메시지와 함께 예외를 생성합니다.
     */
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
    }
}