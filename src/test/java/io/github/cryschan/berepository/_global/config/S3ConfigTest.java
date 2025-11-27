package io.github.cryschan.berepository._global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "cloud.aws.credentials.access-key=test-access-key",
        "cloud.aws.credentials.secret-key=test-secret-key",
        "cloud.aws.region.static=ap-northeast-2"
})
@DisplayName("S3Config 테스트")
class S3ConfigTest {

    @Autowired
    private S3Presigner s3Presigner;

    @Test
    @DisplayName("성공: S3Presigner 빈이 정상적으로 생성된다")
    void s3Presigner_Bean_Created() {
        // then
        assertThat(s3Presigner).isNotNull();
    }

    @Test
    @DisplayName("성공: S3Presigner가 올바른 region으로 설정된다")
    void s3Presigner_Region_Configuration() {
        // then
        assertThat(s3Presigner).isNotNull();
        assertThat(s3Presigner.toString()).contains("ap-northeast-2");
    }
}