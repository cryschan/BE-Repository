package io.github.cryschan.berepository.domain.upload.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UploadResponse DTO 테스트")
class UploadResponseTest {

    @Test
    @DisplayName("성공: presignedUrl과 finalUrl로 UploadResponse를 생성한다")
    void create_UploadResponse() {
        // given
        String presignedUrl = "https://bucket.s3.amazonaws.com/uploads/file.jpg?signature=xyz";
        String finalUrl = "https://bucket.s3.amazonaws.com/uploads/file.jpg";

        // when
        UploadResponse response = new UploadResponse(presignedUrl, finalUrl);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getPresignedUrl()).isEqualTo(presignedUrl);
        assertThat(response.getFinalUrl()).isEqualTo(finalUrl);
    }

    @Test
    @DisplayName("성공: 서로 다른 두 URL을 정확히 구분하여 저장한다")
    void distinguish_Two_Urls() {
        // given
        String presignedUrl = "https://bucket.s3.ap-northeast-2.amazonaws.com/uploads/abc123.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...";
        String finalUrl = "https://bucket.s3.ap-northeast-2.amazonaws.com/uploads/abc123.jpg";

        // when
        UploadResponse response = new UploadResponse(presignedUrl, finalUrl);

        // then
        assertThat(response.getPresignedUrl()).contains("X-Amz-Algorithm");
        assertThat(response.getFinalUrl()).doesNotContain("X-Amz-Algorithm");
        assertThat(response.getPresignedUrl()).isNotEqualTo(response.getFinalUrl());
    }

    @Test
    @DisplayName("성공: UUID를 포함한 고유 파일명의 URL을 저장한다")
    void uuid_In_Filename() {
        // given
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        String presignedUrl = String.format("https://bucket.s3.amazonaws.com/uploads/%s_image.jpg?sig=xyz", uuid);
        String finalUrl = String.format("https://bucket.s3.amazonaws.com/uploads/%s_image.jpg", uuid);

        // when
        UploadResponse response = new UploadResponse(presignedUrl, finalUrl);

        // then
        assertThat(response.getPresignedUrl()).contains(uuid);
        assertThat(response.getFinalUrl()).contains(uuid);
    }

    @Test
    @DisplayName("성공: null 값으로도 객체를 생성할 수 있다")
    void create_With_Null_Values() {
        // when
        UploadResponse response = new UploadResponse(null, null);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getPresignedUrl()).isNull();
        assertThat(response.getFinalUrl()).isNull();
    }
}