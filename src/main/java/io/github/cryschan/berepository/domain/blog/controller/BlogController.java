package io.github.cryschan.berepository.domain.blog.controller;

import io.github.cryschan.berepository._global.exception.dto.ErrorResponse;
import io.github.cryschan.berepository.domain.blog.dto.response.BlogPageResponse;
import io.github.cryschan.berepository.domain.blog.dto.response.BlogResponse;
import io.github.cryschan.berepository.domain.blog.service.BlogService;
import io.github.cryschan.berepository.domain.user.exception.UserException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "블로그", description = "블로그 글 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blogs")
public class BlogController {

    private final BlogService blogService;

    @Operation(summary = "내 블로그 목록 조회", description = "로그인한 유저의 블로그 목록을 페이지네이션하여 조회합니다")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BlogPageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 페이지 번호",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "잘못된 페이지 번호",
                                    value = """
                                            {
                                              "message": "페이지 번호는 1 이상이어야 합니다",
                                              "status": 400,
                                              "code": "BL002",
                                              "timestamp": "2025-11-25T12:24:48.070Z",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = """
                                            {
                                              "message": "인증이 필요합니다",
                                              "status": 401,
                                              "code": "U004",
                                              "timestamp": "2025-11-25T12:24:48.071Z",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "권한 없음",
                                    value = """
                                            {
                                              "message": "접근 권한이 없습니다",
                                              "status": 403,
                                              "code": "U005",
                                              "timestamp": "2025-11-25T12:24:48.071Z",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    public BlogPageResponse getMyBlogs(
            Principal principal,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") int page
    ) {
        Long userId = extractUserId(principal);
        return blogService.getMyBlogs(userId, page);
    }

    @Operation(summary = "블로그 상세 조회", description = "블로그 ID로 상세 정보를 조회합니다")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BlogResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 블로그 ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "잘못된 ID",
                                    value = """
                                            {
                                              "message": "유효하지 않은 블로그 ID입니다",
                                              "status": 400,
                                              "code": "BL003",
                                              "timestamp": "2025-11-25T12:24:48.070Z",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = """
                                            {
                                              "message": "인증이 필요합니다",
                                              "status": 401,
                                              "code": "U004",
                                              "timestamp": "2025-11-25T12:24:48.071Z",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "블로그를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "블로그 없음",
                                    value = """
                                            {
                                              "message": "블로그를 찾을 수 없습니다. id: 999",
                                              "status": 404,
                                              "code": "BL001",
                                              "timestamp": "2025-11-25T12:24:48.070Z",
                                              "errors": []
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{blogId}")
    @ResponseStatus(HttpStatus.OK)
    public BlogResponse getBlog(
            @Parameter(description = "블로그 ID", example = "1")
            @PathVariable Long blogId
    ) {
        return blogService.getBlog(blogId);
    }

    private Long extractUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw UserException.unauthorized("인증이 필요합니다");
        }
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            throw UserException.unauthorized("유효하지 않은 사용자 인증 정보입니다");
        }
    }
}