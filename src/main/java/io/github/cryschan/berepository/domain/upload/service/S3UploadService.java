package io.github.cryschan.berepository.domain.upload.service;

import io.github.cryschan.berepository.domain.upload.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final S3Presigner presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public UploadResponse generatePresignedUrl(String originalFileName, String contentType) {
        // 입력값 검증
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명은 필수입니다");
        }
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Content-Type은 필수입니다");
        }

        // 파일명 처리
        String safeFileName = generateSafeFileName(originalFileName);
        String key = "uploads/" + safeFileName;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        try {
            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(
                    PutObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(10))
                            .putObjectRequest(objectRequest)
                            .build()
            );

            String finalUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);

            log.debug("Presigned URL 생성 완료: key={}", key);

            return new UploadResponse(presignedRequest.url().toString(), finalUrl);
        } catch (Exception e) {
            log.error("S3 presigned URL 생성 실패: key={}, error={}", key, e.getMessage(), e);
            throw new RuntimeException("파일 업로드 URL 생성에 실패했습니다", e);
        }
    }

    /**
     * 안전한 파일명 생성
     * - UUID로 고유성 보장
     * - 확장자만 유지 (원본 파일명은 버림)
     */
    private String generateSafeFileName(String originalFileName) {
        String extension = extractExtension(originalFileName);
        return UUID.randomUUID() + extension;
    }

    /**
     * 파일 확장자 추출
     */
    private String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";  // 확장자 없음
        }
        String ext = fileName.substring(lastDotIndex).toLowerCase();

        // 허용된 이미지 확장자만 허용
        if (isAllowedExtension(ext)) {
            return ext;
        }
        return "";
    }

    /**
     * 허용된 확장자인지 확인
     */
    private boolean isAllowedExtension(String extension) {
        return extension.matches("\\.(jpg|jpeg|png|gif|webp)");
    }
}