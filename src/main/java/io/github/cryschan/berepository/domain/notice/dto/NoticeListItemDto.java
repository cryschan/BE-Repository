package io.github.cryschan.berepository.domain.notice.dto;

import io.github.cryschan.berepository.domain.notice.entity.Notice;

import java.time.LocalDateTime;

public record NoticeListItemDto(
        Long id,
        String title,
        LocalDateTime createdAt,
        boolean isNew,
        boolean isImportant
) {

    private static final int NEW_NOTICE_DAYS = 3;

    public static NoticeListItemDto from(Notice notice) {
        return new NoticeListItemDto(
                notice.getId(),
                notice.getTitle(),
                notice.getCreatedAt(),
                isNewNotice(notice.getCreatedAt()),
                notice.isImportant()
        );
    }

    private static boolean isNewNotice(LocalDateTime createdAt) {
        return createdAt.isAfter(LocalDateTime.now().minusDays(NEW_NOTICE_DAYS));
    }

}
