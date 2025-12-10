package io.github.cryschan.berepository.domain.inquiry.dto.Response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 관리자 문의 목록에 사용되는 페이지 응답
 */
@Getter
@Builder
@Schema(description = "관리자 문의 페이지 응답")
public class AdminInquiryPageResponse {

    @Schema(description = "문의 목록")
    private List<AdminInquiryListResponse> inquiries;

    @Schema(description = "현재 페이지 (1부터 시작)", example = "1")
    private int currentPage;

    @Schema(description = "전체 페이지 수", example = "3")
    private int totalPages;

    @Schema(description = "전체 문의 수", example = "12")
    private long totalElements;

    @Schema(description = "페이지당 문의 수", example = "10")
    private int size;

    @Schema(description = "첫 페이지 여부", example = "true")
    private boolean first;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean last;

    public static AdminInquiryPageResponse from(Page<AdminInquiryListResponse> page) {
        return AdminInquiryPageResponse.builder()
                .inquiries(page.getContent())
                .currentPage(page.getNumber() + 1)
                .totalPages(Math.max(page.getTotalPages(), 1))
                .totalElements(page.getTotalElements())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
