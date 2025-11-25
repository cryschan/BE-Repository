package io.github.cryschan.berepository._global.exception.base;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ==================== User 도메인 (U로 시작) ====================
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    USER_DUPLICATION("U002", "이미 존재하는 사용자입니다", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("U003", "이메일 혹은 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("U004", "인증이 필요합니다", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("U005", "접근 권한이 없습니다", HttpStatus.FORBIDDEN),

    // ==================== Board 도메인 (B로 시작) ====================
    BOARD_NOT_FOUND("B001", "게시글을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    BOARD_ACCESS_DENIED("B002", "게시글에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),

    // ==================== FAQ 도메인 (F로 시작) ====================
    FAQ_NOT_FOUND("F001", "FAQ를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    FAQ_ACCESS_DENIED("F002", "FAQ에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),

    // ==================== BlogTemplate 도메인 (BT로 시작) ====================
    BLOG_TEMPLATE_NOT_FOUND("BT001", "블로그 템플릿을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    BLOG_TEMPLATE_ACCESS_DENIED("BT002", "블로그 템플릿에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),
    BLOG_TEMPLATE_ALREADY_EXISTS("BT003", "이미 블로그 템플릿이 존재합니다", HttpStatus.CONFLICT),

    // ==================== 공통 (C로 시작) ====================
    INVALID_INPUT("C001", "입력값이 올바르지 않습니다", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST("C002", "잘못된 요청입니다", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("C003", "리소스를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DATA_INTEGRITY_VIOLATION("C004", "데이터 무결성 제약 조건 위반", HttpStatus.CONFLICT),
    METHOD_NOT_ALLOWED("C005", "허용되지 않은 HTTP 메서드입니다", HttpStatus.METHOD_NOT_ALLOWED),
    INTERNAL_SERVER_ERROR("C999", "서버 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}