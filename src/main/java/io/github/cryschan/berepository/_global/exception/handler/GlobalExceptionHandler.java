package io.github.cryschan.berepository._global.exception.handler;

import io.github.cryschan.berepository._global.exception.base.BaseException;
import io.github.cryschan.berepository._global.exception.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

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
        log.error("MethodArgumentNotValidException: {}", e.getMessage());

        BindingResult bindingResult = e.getBindingResult();
        List<ErrorResponse.FieldError> fieldErrors = bindingResult.getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.FieldError(
                        error.getField(),
                        error.getRejectedValue() != null ? error.getRejectedValue().toString() : "",
                        error.getDefaultMessage()
                ))
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        "입력값 검증에 실패했습니다.",
                        HttpStatus.BAD_REQUEST.value(),
                        "C001",
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

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        "서버 내부 오류가 발생했습니다.",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "C999"
                ));
    }
}