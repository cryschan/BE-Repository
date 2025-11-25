package io.github.cryschan.berepository.domain.blog.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

public class BlogException extends DomainException {

    private BlogException(ErrorCode errorCode) {
        super(errorCode);
    }

    private BlogException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    private BlogException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    private BlogException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    // 404 - 블로그를 찾을 수 없음
    public static BlogException notFound(String message) {
        return new BlogException(ErrorCode.BLOG_NOT_FOUND, message);
    }

    public static BlogException notFound() {
        return new BlogException(ErrorCode.BLOG_NOT_FOUND);
    }

    // 400 - 잘못된 페이지 번호
    public static BlogException invalidPage(String message) {
        return new BlogException(ErrorCode.INVALID_PAGE_NUMBER, message);
    }

    public static BlogException invalidPage() {
        return new BlogException(ErrorCode.INVALID_PAGE_NUMBER);
    }

    // 400 - 잘못된 블로그 ID
    public static BlogException invalidId(String message) {
        return new BlogException(ErrorCode.INVALID_BLOG_ID, message);
    }

    public static BlogException invalidId() {
        return new BlogException(ErrorCode.INVALID_BLOG_ID);
    }

    // 403 - 접근 권한 없음
    public static BlogException forbidden(String message) {
        return new BlogException(ErrorCode.BLOG_ACCESS_DENIED, message);
    }

    public static BlogException forbidden() {
        return new BlogException(ErrorCode.BLOG_ACCESS_DENIED);
    }

    // 400 - 잘못된 요청
    public static BlogException badRequest(String message) {
        return new BlogException(ErrorCode.INVALID_BLOG_REQUEST, message);
    }

    public static BlogException badRequest() {
        return new BlogException(ErrorCode.INVALID_BLOG_REQUEST);
    }
}