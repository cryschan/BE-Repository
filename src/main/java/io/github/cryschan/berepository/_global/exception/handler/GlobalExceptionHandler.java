package io.github.cryschan.berepository._global.exception.handler;

import io.github.cryschan.berepository._global.exception.dto.ErrorResponse;
import io.github.cryschan.berepository.domain.user.exception.DuplicationUserException;
import io.github.cryschan.berepository.domain.user.exception.InvalidCredentialsException;
import io.github.cryschan.berepository.domain.user.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
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
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 1. 사용자를 찾을 수 없음
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public ErrorResponse handleUserNotFoundException(UserNotFoundException e) {
        log.error("UserNotFoundException: {}", e.getMessage());
        return ErrorResponse.of(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                "USER_NOT_FOUND"
        );
    }

    // 2. 이메일 중복
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicationUserException.class)
    public ErrorResponse handleDuplicationUserException(DuplicationUserException e) {
        log.error("DuplicationUserException: {}", e.getMessage());
        return ErrorResponse.of(
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                "DUPLICATE_EMAIL"
        );
    }

    // 3. 로그인 실패 (이메일/비밀번호 오류)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidCredentialsException.class)
    public ErrorResponse handleInvalidCredentialsException(InvalidCredentialsException e) {
        log.error("InvalidCredentialsException: {}", e.getMessage());
        return ErrorResponse.of(
                e.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                "INVALID_CREDENTIALS"
        );
    }

    // 4. 입력 검증 실패 (@Valid)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException e) {
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

        return ErrorResponse.of(
                "입력값 검증에 실패했습니다.",
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                fieldErrors
        );
    }

    // 5. 일반적인 예외 (fallback)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        return ErrorResponse.of(
                "서버 내부 오류가 발생했습니다.",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR"
        );
    }
}