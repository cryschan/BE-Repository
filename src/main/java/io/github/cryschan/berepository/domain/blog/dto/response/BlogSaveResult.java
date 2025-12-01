package io.github.cryschan.berepository.domain.blog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 블로그 일괄 저장 결과
 */
@Schema(description = "블로그 일괄 저장 결과")
public record BlogSaveResult(
        @Schema(description = "저장 성공 수", example = "3")
        int successCount,

        @Schema(description = "저장 실패 수", example = "1")
        int failCount
) {
    public static BlogSaveResult of(int successCount, int failCount) {
        return new BlogSaveResult(successCount, failCount);
    }

    /**
     * 전체 처리 건수
     */
    public int totalCount() {
        return successCount + failCount;
    }

    /**
     * 모든 저장이 성공했는지 여부
     */
    public boolean isAllSuccess() {
        return failCount == 0;
    }
}
