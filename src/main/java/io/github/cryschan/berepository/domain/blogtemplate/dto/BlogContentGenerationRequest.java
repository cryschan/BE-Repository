package io.github.cryschan.berepository.domain.blogtemplate.dto;

import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import lombok.Builder;

import java.util.List;

/**
 * 블로그 컨텐츠 생성을 위한 요청 DTO
 * 템플릿 설정과 크롤링된 상품 정보를 포함
 */
@Builder
public record BlogContentGenerationRequest(
        // 사용자 정보
        Long userId,
        String templateTitle,

        // 템플릿 설정
        int charLimit,              // 글자 수 제한
        boolean includeImages,       // 이미지 포함 여부
        int imageCount,              // 이미지 개수
        List<String> platforms,      // 발행할 플랫폼 목록

        // 크롤링된 상품 정보
        List<SsadaguProductDto> crawledProducts
) {
}
