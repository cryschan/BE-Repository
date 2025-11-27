package io.github.cryschan.berepository.domain.upload.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UploadRequest DTO 테스트")
class UploadRequestTest {

    @Test
    @DisplayName("성공: UploadRequest 객체를 생성한다")
    void create_UploadRequest() {
        // given
        UploadRequest request = new UploadRequest();

        // then
        assertThat(request).isNotNull();
        assertThat(request.getFileName()).isNull();
        assertThat(request.getContentType()).isNull();
    }

    @Test
    @DisplayName("성공: fileName과 contentType을 설정하고 조회한다")
    void set_And_Get_Fields() throws Exception {
        // given
        UploadRequest request = new UploadRequest();

        // when
        setField(request, "fileName", "test-image.jpg");
        setField(request, "contentType", "image/jpeg");

        // then
        assertThat(request.getFileName()).isEqualTo("test-image.jpg");
        assertThat(request.getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("성공: PNG 파일 정보를 담는다")
    void png_File_Info() throws Exception {
        // given
        UploadRequest request = new UploadRequest();

        // when
        setField(request, "fileName", "screenshot.png");
        setField(request, "contentType", "image/png");

        // then
        assertThat(request.getFileName()).isEqualTo("screenshot.png");
        assertThat(request.getContentType()).isEqualTo("image/png");
    }

    @Test
    @DisplayName("성공: 긴 파일명을 담는다")
    void long_FileName() throws Exception {
        // given
        UploadRequest request = new UploadRequest();
        String longFileName = "very-long-file-name-with-lots-of-characters-and-description-2024-01-01.jpg";

        // when
        setField(request, "fileName", longFileName);
        setField(request, "contentType", "image/jpeg");

        // then
        assertThat(request.getFileName()).isEqualTo(longFileName);
        assertThat(request.getContentType()).isEqualTo("image/jpeg");
    }

    private void setField(Object object, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}