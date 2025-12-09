package io.github.cryschan.berepository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("DB 연결 문제로 통합 테스트 비활성화 - 로컬 PostgreSQL 필요")
@Deprecated
@SpringBootTest
class BeRepositoryApplicationTests {

    @Test
    void contextLoads() {
    }

}
