package io.github.cryschan.berepository._global.exception.handler;

import io.github.cryschan.berepository._global.exception.base.BaseException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;
import io.github.cryschan.berepository._global.exception.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 애플리케이션 전역 예외를 처리하는 핸들러 클래스입니다.
 * <p>
 * {@code @RestControllerAdvice}를 통해 모든 컨트롤러에서 발생하는 예외를 중앙 집중식으로 처리합니다.
 * 각 예외 타입에 맞는 HTTP 상태 코드와 통일된 형식의 오류 응답(ErrorResponse)을 반환합니다.
 * </p>
 *
 * @author cryschan
 * @since 1.0
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BaseException 및 그 하위 예외들을 일괄 처리합니다.
     * 모든 비즈니스 예외는 이 핸들러를 통해 처리됩니다.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        log.error("{}[{}]: {}", e.getClass().getSimpleName(), e.getCode(), e.getMessage());

        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ErrorResponse.of(
                        e.getMessage(),
                        e.getHttpStatusCode(),
                        e.getCode()
                ));
    }

    /**
     * 입력 검증 실패 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_INPUT;

        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.FieldError(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        String combinedMessage = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(
                        combinedMessage,
                        errorCode.getHttpStatusCode(),
                        errorCode.getCode(),
                        fieldErrors
                ));
    }

    /**
     * 일반적인 예외 (fallback)
     * 예상하지 못한 예외 발생 시 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected Exception: {}", e.getMessage(), e);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(
                        errorCode.getMessage(),
                        errorCode.getHttpStatusCode(),
                        errorCode.getCode()
                ));
    }
}