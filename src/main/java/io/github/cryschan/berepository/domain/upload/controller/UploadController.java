package io.github.cryschan.berepository.domain.upload.controller;

import io.github.cryschan.berepository._global.exception.dto.ErrorResponse;
import io.github.cryschan.berepository.domain.upload.dto.UploadRequest;
import io.github.cryschan.berepository.domain.upload.dto.UploadResponse;
import io.github.cryschan.berepository.domain.upload.service.S3UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "이미지 업로드", description = "S3 이미지 업로드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uploads")
public class UploadController {

    private final S3UploadService s3UploadService;

    @Operation(summary = "Presigned URL 발급", description = "S3 이미지 업로드를 위한 Presigned URL을 발급합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UploadResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "잘못된 파일 형식",
                                    value = """
                                            {
                                              "message": "지원하지 않는 파일 형식입니다",
                                              "status": 400,
                                              "code": "UP001",
                                              "timestamp": "2025-11-25T12:24:48.070Z",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public UploadResponse getPresignedUrl(@RequestBody UploadRequest request) {
        return s3UploadService.generatePresignedUrl(
                request.getFileName(),
                request.getContentType()
        );
    }
}
