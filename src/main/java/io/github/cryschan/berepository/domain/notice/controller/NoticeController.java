package io.github.cryschan.berepository.domain.notice.controller;

import io.github.cryschan.berepository.domain.notice.dto.NoticeDetailDto;
import io.github.cryschan.berepository.domain.notice.dto.NoticeListItemDto;
import io.github.cryschan.berepository.domain.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공지사항", description = "공지사항 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    // 공지사항 목록 조회
    @Operation(summary = "공지사항 목록 조회", description = "공지사항 목록 데이터를 조회합니다 (모든 사용자 접근 가능)")
    @GetMapping
    public ResponseEntity<Page<NoticeListItemDto>> getNotices(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        Page<NoticeListItemDto> notices = noticeService.getNoticeList(page, size);
        return ResponseEntity.ok(notices);
    }

    // 공지사항 단건 조회
    @Operation(summary = "공지사항 상세 조회", description = "공지사항 상세 데이터를 조회합니다 (모든 사용자 접근 가능)")
    @GetMapping("/{id}")
    public ResponseEntity<NoticeDetailDto> getNotice(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        NoticeDetailDto notice = noticeService.getNotice(id, userId);
        return ResponseEntity.ok(notice);
    }

}
