package io.github.cryschan.berepository.domain.notice.dto;

import io.github.cryschan.berepository.domain.notice.entity.Notice;

import java.time.LocalDateTime;

public record NoticeDetailDto(
        Long id,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean isNew,
        boolean isImportant,
        boolean canEdit     // 수정/삭제 버튼 노출 여부
) {
    private static final int NEW_NOTICE_DAYS = 3;

    public static NoticeDetailDto from(Notice notice, boolean canEdit) {
        return new NoticeDetailDto(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getCreatedAt(),
                notice.getUpdatedAt(),
                isNewNotice(notice.getCreatedAt()),
                notice.isImportant(),
                canEdit
        );
    }

    private static boolean isNewNotice(LocalDateTime createdAt) {
        return createdAt.isAfter(LocalDateTime.now().minusDays(NEW_NOTICE_DAYS));
    }
}
