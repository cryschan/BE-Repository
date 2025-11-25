package io.github.cryschan.berepository.domain.blogtemplate.exception;

import io.github.cryschan.berepository._global.exception.base.DomainException;
import io.github.cryschan.berepository._global.exception.base.ErrorCode;

/**
 * BlogTemplate 도메인 통합 예외 클래스
 * BlogTemplate 도메인에서 발생하는 모든 예외를 정적 팩토리 메서드로 제공합니다.
 */
public class BlogTemplateException extends DomainException {

    protected BlogTemplateException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected BlogTemplateException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    protected BlogTemplateException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    protected BlogTemplateException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    // ==================== 정적 팩토리 메서드 ====================

    /**
     * 블로그 템플릿을 찾을 수 없을 때 발생하는 예외
     */
    public static BlogTemplateException notFound(Long templateId) {
        return new BlogTemplateException(
            ErrorCode.BLOG_TEMPLATE_NOT_FOUND,
            String.format("블로그 템플릿을 찾을 수 없습니다. ID: %d", templateId)
        );
    }

    /**
     * 사용자 ID로 블로그 템플릿을 찾을 수 없을 때 발생하는 예외
     */
    public static BlogTemplateException notFoundByUserId(Long userId) {
        return new BlogTemplateException(
            ErrorCode.BLOG_TEMPLATE_NOT_FOUND,
            String.format("블로그 템플릿을 찾을 수 없습니다. User ID: %d", userId)
        );
    }

    /**
     * 일반적인 블로그 템플릿 찾기 실패 예외
     */
    public static BlogTemplateException notFound() {
        return new BlogTemplateException(ErrorCode.BLOG_TEMPLATE_NOT_FOUND);
    }

    /**
     * 블로그 템플릿이 이미 존재할 때 발생하는 예외
     */
    public static BlogTemplateException alreadyExists(Long userId) {
        return new BlogTemplateException(
            ErrorCode.BLOG_TEMPLATE_ALREADY_EXISTS,
            String.format("이미 블로그 템플릿이 존재합니다. User ID: %d", userId)
        );
    }

    /**
     * 블로그 템플릿이 이미 존재할 때 발생하는 예외 (일반)
     */
    public static BlogTemplateException alreadyExists() {
        return new BlogTemplateException(ErrorCode.BLOG_TEMPLATE_ALREADY_EXISTS);
    }

    /**
     * 블로그 템플릿에 대한 접근 권한이 없을 때 발생하는 예외
     */
    public static BlogTemplateException accessDenied() {
        return new BlogTemplateException(ErrorCode.BLOG_TEMPLATE_ACCESS_DENIED);
    }

    /**
     * 블로그 템플릿에 대한 접근 권한이 없을 때 발생하는 예외 (템플릿 ID 명시)
     */
    public static BlogTemplateException accessDenied(Long templateId) {
        return new BlogTemplateException(
            ErrorCode.BLOG_TEMPLATE_ACCESS_DENIED,
            String.format("블로그 템플릿에 대한 권한이 없습니다. Template ID: %d", templateId)
        );
    }

    /**
     * 블로그 템플릿에 대한 접근 권한이 없을 때 발생하는 예외 (커스텀 메시지)
     */
    public static BlogTemplateException accessDenied(String message) {
        return new BlogTemplateException(ErrorCode.BLOG_TEMPLATE_ACCESS_DENIED, message);
    }

    /**
     * 이미지 옵션이 잘못되었을 때 발생하는 예외
     */
    public static BlogTemplateException invalidImageOptions(int imageCount, boolean includeImages) {
        String message = includeImages
                ? String.format("이미지 포함 시 imageCount는 1~10이어야 합니다. 현재 값: %d", imageCount)
                : String.format("이미지 미포함 시 imageCount는 0이어야 합니다. 현재 값: %d", imageCount);
        return new BlogTemplateException(ErrorCode.INVALID_INPUT, message);
    }
}
