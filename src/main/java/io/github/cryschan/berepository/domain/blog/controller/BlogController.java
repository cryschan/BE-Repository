package io.github.cryschan.berepository.domain.blog.controller;

import io.github.cryschan.berepository._global.exception.dto.ErrorResponse;
import io.github.cryschan.berepository.domain.blog.dto.response.BlogPageResponse;
import io.github.cryschan.berepository.domain.blog.dto.response.BlogResponse;
import io.github.cryschan.berepository.domain.blog.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Blog", description = "블로그 API")
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
                    content = @Content(schema = @Schema(implementation = BlogPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/my")
    public ResponseEntity<BlogPageResponse> getMyBlogs(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") int page
    ) {
        Long userId = 1L; // TODO: Long userId = userDetails.getUserId();
        BlogPageResponse response = blogService.getMyBlogs(userId, page);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "블로그 상세 조회", description = "블로그 ID로 상세 정보를 조회합니다")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = BlogResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "블로그를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{blogId}")
    public ResponseEntity<BlogResponse> getBlog(
            @Parameter(description = "블로그 ID", example = "1")
            @PathVariable Long blogId
    ) {
        BlogResponse blog = blogService.getBlog(blogId);
        return ResponseEntity.ok(blog);
    }
}