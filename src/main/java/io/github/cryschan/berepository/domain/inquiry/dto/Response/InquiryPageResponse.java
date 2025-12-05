package io.github.cryschan.berepository.domain.inquiry.dto.Response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "문의 페이지 응답")
@Getter
@Builder
public class InquiryPageResponse {

    @Schema(description = "문의 목록")
    private List<InquiryListResponse> inquiries;

    @Schema(description = "현재 페이지 (1부터 시작)", example = "1")
    private int currentPage;

    @Schema(description = "전체 페이지 수", example = "3")
    private int totalPages;

    @Schema(description = "전체 문의 수", example = "12")
    private long totalElements;

    @Schema(description = "페이지당 문의 수", example = "10")
    private int size;

    @Schema(description = "첫 페이지 여부", example = "true")
    private boolean isFirst;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean isLast;

    public static InquiryPageResponse from(Page<InquiryListResponse> page) {
        return InquiryPageResponse.builder()
                .inquiries(page.getContent())
                .currentPage(page.getNumber() + 1)  // 0-based → 1-based
                .totalPages(Math.max(page.getTotalPages(), 1))  // 최소 1페이지
                .totalElements(page.getTotalElements())
                .size(page.getSize())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}