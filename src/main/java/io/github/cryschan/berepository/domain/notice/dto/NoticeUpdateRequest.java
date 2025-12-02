package io.github.cryschan.berepository.domain.notice.dto;

public record NoticeUpdateRequest(
        String title,
        String content,
        boolean isImportant
) {
}
