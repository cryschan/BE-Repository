package io.github.cryschan.berepository._global.exception.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 오류 응답을 위한 공통 DTO입니다.
 * <p>
 * 모든 예외 처리 시 일관된 형식으로 오류 정보를 클라이언트에게 전달합니다.
 * 입력값 검증 실패 시 필드별 오류 정보(FieldError)도 포함할 수 있습니다.
 * </p>
 *
 * @param message   오류 메시지
 * @param status    HTTP 상태 코드
 * @param code      애플리케이션 오류 코드
 * @param timestamp 오류 발생 시각
 * @param errors    필드별 검증 오류 목록 (선택)
 * @author cryschan
 * @since 1.0
 */
public record ErrorResponse(
        String message,
        int status,
        String code,
        LocalDateTime timestamp,
        List<FieldError> errors
) {

    /**
     * 기본 오류 응답을 생성합니다.
     *
     * @param message 오류 메시지
     * @param status  HTTP 상태 코드
     * @param code    애플리케이션 오류 코드
     * @return ErrorResponse 인스턴스
     */
    public static ErrorResponse of(String message, int status, String code) {
        return new ErrorResponse(
                message,
                status,
                code,
                LocalDateTime.now(),
                null);
    }

    /**
     * 필드 검증 오류 정보를 포함한 오류 응답을 생성합니다.
     *
     * @param message 오류 메시지
     * @param status  HTTP 상태 코드
     * @param code    애플리케이션 오류 코드
     * @param errors  필드별 검증 오류 목록
     * @return ErrorResponse 인스턴스
     */
    public static ErrorResponse of(String message, int status, String code, List<FieldError> errors) {
        return new ErrorResponse(
                message,
                status,
                code,
                LocalDateTime.now(),
                errors);
    }

    /**
     * 입력값 검증 실패 시 필드별 오류 정보를 담는 DTO입니다.
     *
     * @param field   오류가 발생한 필드명
     * @param message 오류 메시지
     */
    public record FieldError(
            String field,
            String message
    ) {
    }
}