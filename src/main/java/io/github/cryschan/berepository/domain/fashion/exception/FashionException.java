package io.github.cryschan.berepository.domain.fashion.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

/**
 * Fashion 도메인 통합 예외 클래스
 * Fashion 도메인에서 발생하는 모든 예외를 정적 팩토리 메서드로 제공합니다.
 */
public class FashionException extends DomainException {

    protected FashionException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected FashionException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    protected FashionException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    protected FashionException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    // ==================== 정적 팩토리 메서드 ====================

    /**
     * 크롤링이 실패했을 때 발생하는 예외
     */
    public static FashionException crawlingFailed(String target) {
        return new FashionException(
                ErrorCode.CRAWLING_FAILED,
                String.format("크롤링에 실패했습니다: %s", target)
        );
    }

    /**
     * 크롤링이 실패했을 때 발생하는 예외 (원인 포함)
     */
    public static FashionException crawlingFailed(String target, Throwable cause) {
        return new FashionException(
                ErrorCode.CRAWLING_FAILED,
                String.format("크롤링에 실패했습니다: %s", target),
                cause
        );
    }

    /**
     * 카테고리가 필수인데 제공되지 않았을 때 발생하는 예외
     */
    public static FashionException categoryRequired() {
        return new FashionException(
                ErrorCode.CATEGORY_REQUIRED,
                "카테고리는 필수입니다"
        );
    }

    /**
     * 상품을 찾을 수 없을 때 발생하는 예외
     */
    public static FashionException noProductFound(String category) {
        return new FashionException(
                ErrorCode.NO_PRODUCT_FOUND,
                String.format("'%s' 카테고리에서 상품을 찾을 수 없습니다", category)
        );
    }

    /**
     * 상품을 찾을 수 없을 때 발생하는 예외 (일반)
     */
    public static FashionException noProductFound() {
        return new FashionException(ErrorCode.NO_PRODUCT_FOUND);
    }

    /**
     * 유효하지 않은 카테고리일 때 발생하는 예외
     */
    public static FashionException invalidCategory(String category) {
        return new FashionException(
                ErrorCode.INVALID_CATEGORY,
                String.format("유효하지 않은 카테고리입니다: %s", category)
        );
    }
}
