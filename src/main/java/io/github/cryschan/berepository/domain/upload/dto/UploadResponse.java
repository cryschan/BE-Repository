package io.github.cryschan.berepository.domain.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadResponse {
    private String presignedUrl;  // S3에 PUT할 URL
    private String finalUrl;      // 업로드 완료 후 공개 URL
}