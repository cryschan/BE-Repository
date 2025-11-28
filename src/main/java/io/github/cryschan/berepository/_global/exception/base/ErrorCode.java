package io.github.cryschan.berepository._global.exception.base;

import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 전역 에러 코드를 정의하는 열거형
 * 도메인별로 에러 코드를 분류하여 관리합니다.
 */
public enum ErrorCode {

    // ==================== User 도메인 (U로 시작) ====================
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    USER_DUPLICATION("U002", "이미 존재하는 사용자입니다", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("U003", "이메일 혹은 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("U004", "인증이 필요합니다", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("U005", "접근 권한이 없습니다", HttpStatus.FORBIDDEN),

    // ==================== Token 도메인 (T로 시작) ====================
    INVALID_TOKEN("T001", "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("T002", "만료된 토큰입니다", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FOUND("T003", "토큰을 찾을 수 없습니다", HttpStatus.NOT_FOUND),

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

    // ==================== Blog 도메인 (BL로 시작) ====================
    BLOG_NOT_FOUND("BL001", "블로그를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_PAGE_NUMBER("BL002", "페이지 번호는 1 이상이어야 합니다", HttpStatus.BAD_REQUEST),
    INVALID_BLOG_ID("BL003", "유효하지 않은 블로그 ID입니다", HttpStatus.BAD_REQUEST),
    INVALID_BLOG_REQUEST("BL004", "잘못된 블로그 요청입니다", HttpStatus.BAD_REQUEST),
    BLOG_ACCESS_DENIED("BL005", "이 블로그에 접근할 권한이 없습니다", HttpStatus.FORBIDDEN),

    // ==================== AI 도메인 (AI로 시작) ====================
    AI_INVALID_PRODUCT("AI001", "유효하지 않은 상품 정보입니다", HttpStatus.BAD_REQUEST),
    AI_EMPTY_PRODUCT_LIST("AI002", "비교할 상품이 없습니다", HttpStatus.BAD_REQUEST),
    AI_INSUFFICIENT_PRODUCTS("AI003", "비교하려면 2개 이상의 상품이 필요합니다", HttpStatus.BAD_REQUEST),
  
    // ==================== Fashion 도메인 (FS로 시작) ====================
    CRAWLING_FAILED("FS001", "크롤링에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    CATEGORY_REQUIRED("FS002", "카테고리는 필수입니다", HttpStatus.BAD_REQUEST),
    NO_PRODUCT_FOUND("FS003", "상품을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_CATEGORY("FS004", "유효하지 않은 카테고리입니다", HttpStatus.BAD_REQUEST),

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