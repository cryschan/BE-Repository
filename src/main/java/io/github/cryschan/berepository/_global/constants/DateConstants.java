package io.github.cryschan.berepository._global.constants;

import java.time.format.DateTimeFormatter;

/**
 * 날짜 관련 공통 상수
 */
public final class DateConstants {

    private DateConstants() {
        // 인스턴스 생성 방지
    }

    /**
     * 블로그 제목에 사용되는 날짜 포맷 (yyyy.MM.dd)
     */
    public static final DateTimeFormatter BLOG_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd");
}
