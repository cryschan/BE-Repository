package io.github.cryschan.berepository.domain.ai.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

/**
 * AI 도메인 통합 예외 클래스
 * AI 도메인에서 발생하는 모든 예외를 정적 팩토리 메서드로 제공합니다.
 */
public class AiException extends DomainException {

    protected AiException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected AiException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    protected AiException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    protected AiException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    // ==================== 정적 팩토리 메서드 ====================

    /**
     * 유효하지 않은 상품 정보일 때 발생하는 예외 (null 체크)
     */
    public static AiException invalidProduct() {
        return new AiException(
                ErrorCode.AI_INVALID_PRODUCT,
                "상품 정보가 null입니다"
        );
    }

    /**
     * 유효하지 않은 상품 정보일 때 발생하는 예외 (커스텀 메시지)
     */
    public static AiException invalidProduct(String message) {
        return new AiException(ErrorCode.AI_INVALID_PRODUCT, message);
    }

    /**
     * 비교할 상품 리스트가 비어있을 때 발생하는 예외
     */
    public static AiException emptyProductList() {
        return new AiException(
                ErrorCode.AI_EMPTY_PRODUCT_LIST,
                "비교할 상품 리스트가 비어있습니다"
        );
    }

    /**
     * 상품이 2개 미만일 때 발생하는 예외
     */
    public static AiException insufficientProducts() {
        return new AiException(
                ErrorCode.AI_INSUFFICIENT_PRODUCTS,
                "상품 비교를 위해서는 최소 2개 이상의 상품이 필요합니다"
        );
    }

    /**
     * 상품이 2개 미만일 때 발생하는 예외 (현재 개수 포함)
     */
    public static AiException insufficientProducts(int count) {
        return new AiException(
                ErrorCode.AI_INSUFFICIENT_PRODUCTS,
                String.format("상품 비교를 위해서는 최소 2개 이상의 상품이 필요합니다. 현재: %d개", count)
        );
    }
}
