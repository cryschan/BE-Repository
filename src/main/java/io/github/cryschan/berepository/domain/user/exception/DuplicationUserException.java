package io.github.cryschan.berepository.domain.user.exception;

/**
 * 이미 등록된 이메일로 회원가입을 시도할 때 발생하는 예외입니다.
 * <p>
 * 이메일은 시스템에서 고유해야 하며, 중복된 이메일로 가입할 수 없습니다.
 * HTTP 409 (Conflict) 응답으로 변환됩니다.
 * </p>
 *
 * @author cryschan
 * @since 1.0
 */
public class DuplicationUserException extends RuntimeException {
    
    /**
     * 중복된 이메일과 함께 예외를 생성합니다.
     *
     * @param email 중복된 이메일 주소
     */
    public DuplicationUserException(String email) {
        super("이미 사용 중인 이메일입니다: " + email);
    }

    /**
     * 기본 예외 메시지와 함께 예외를 생성합니다.
     */
    public DuplicationUserException() {
        super("이미 사용 중인 이메일입니다.");
    }
}