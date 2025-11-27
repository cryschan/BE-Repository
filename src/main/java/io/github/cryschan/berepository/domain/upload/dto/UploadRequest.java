package io.github.cryschan.berepository.domain.upload.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UploadRequest {
    private String fileName;
    private String contentType;
}