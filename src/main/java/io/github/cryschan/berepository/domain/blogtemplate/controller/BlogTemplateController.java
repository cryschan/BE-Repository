package io.github.cryschan.berepository.domain.blogtemplate.controller;

import io.github.cryschan.berepository.domain.blogtemplate.dto.request.BlogTemplateCreateRequest;
import io.github.cryschan.berepository.domain.blogtemplate.dto.request.BlogTemplateUpdateRequest;
import io.github.cryschan.berepository.domain.blogtemplate.dto.response.BlogTemplateResponse;
import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.service.BlogTemplateService;
import io.github.cryschan.berepository.domain.user.exception.UserException;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.List;
import java.security.Principal;

@Tag(name = "블로그 템플릿", description = "블로그 템플릿 관리 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog-templates")
public class BlogTemplateController {

    private final BlogTemplateService blogTemplateService;
    private final UserRepository userRepository;

    @Operation(summary = "블로그 템플릿 생성", description = "새로운 블로그 템플릿을 생성합니다. 한 사용자당 하나의 템플릿만 생성 가능합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "템플릿 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BlogTemplateResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 입력값",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "카테고리는 필수입니다",
                                                "status": 400,
                                                "code": "C001",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": [
                                                    {
                                                        "field": "categories",
                                                        "message": "카테고리는 필수입니다"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "인증이 필요합니다",
                                                "status": 401,
                                                "code": "U004",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "사용자를 찾을 수 없습니다. ID: 999",
                                                "status": 404,
                                                "code": "U001",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "템플릿 중복 (이미 템플릿이 존재)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "이미 블로그 템플릿이 존재합니다. User ID: 1",
                                                "status": 409,
                                                "code": "BT003",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<BlogTemplateResponse> createTemplate(
            @Valid @RequestBody BlogTemplateCreateRequest request,
            Principal principal
    ) {
        Long userId = extractUserId(principal);
        String username = userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound(userId))
                .getUsername();
        BlogTemplateResponse saved = blogTemplateService.createTemplateResponse(userId, request.toEntity(userId, username));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "블로그 템플릿 수정", description = "기존 블로그 템플릿을 수정합니다. 본인의 템플릿만 수정 가능합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "템플릿 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BlogTemplateResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 입력값",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "입력값이 올바르지 않습니다",
                                                "status": 400,
                                                "code": "C001",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "인증이 필요합니다",
                                                "status": 401,
                                                "code": "U004",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (다른 사용자의 템플릿)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "블로그 템플릿에 대한 권한이 없습니다. Template ID: 1",
                                                "status": 403,
                                                "code": "BT002",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "템플릿을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "블로그 템플릿을 찾을 수 없습니다. ID: 999",
                                                "status": 404,
                                                "code": "BT001",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            )
    })
    @PutMapping("/{templateId}")
    public BlogTemplateResponse updateTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody BlogTemplateUpdateRequest request,
            Principal principal
    ) {
        Long userId = extractUserId(principal);
        return blogTemplateService.updateTemplateResponse(
                userId,
                templateId,
                request.title(),
                request.categoriesCopy(),
                request.platformsCopy(),
                request.shopUrl(),
                request.includeImages(),
                request.imageCount(),
                request.charLimit(),
                request.dailyPostTime()
        );
    }

    @Operation(summary = "블로그 템플릿 삭제", description = "블로그 템플릿을 삭제합니다. 본인의 템플릿만 삭제 가능합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "템플릿 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "인증이 필요합니다",
                                                "status": 401,
                                                "code": "U004",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (다른 사용자의 템플릿)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "블로그 템플릿에 대한 권한이 없습니다. Template ID: 1",
                                                "status": 403,
                                                "code": "BT002",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "템플릿을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "블로그 템플릿을 찾을 수 없습니다. ID: 999",
                                                "status": 404,
                                                "code": "BT001",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            )
    })
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long templateId, Principal principal) {
        Long userId = extractUserId(principal);
        blogTemplateService.deleteTemplate(templateId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "블로그 템플릿 단건 조회", description = "ID로 특정 블로그 템플릿을 조회합니다. (인증 필요)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BlogTemplateResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "인증이 필요합니다",
                                                "status": 401,
                                                "code": "U004",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "템플릿을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "블로그 템플릿을 찾을 수 없습니다. ID: 999",
                                                "status": 404,
                                                "code": "BT001",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{templateId}")
    public BlogTemplateResponse getTemplate(@PathVariable Long templateId) {
        return blogTemplateService.getTemplateResponse(templateId);
    }

    @Operation(summary = "전체 블로그 템플릿 조회", description = "모든 블로그 템플릿을 조회합니다. (인증 필요)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BlogTemplateResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "인증이 필요합니다",
                                                "status": 401,
                                                "code": "U004",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public List<BlogTemplateResponse> getAllTemplates() {
        return blogTemplateService.getAllTemplateResponses();
    }

    @Operation(summary = "내 블로그 템플릿 조회", description = "현재 로그인한 사용자의 블로그 템플릿을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BlogTemplateResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "인증이 필요합니다",
                                                "status": 401,
                                                "code": "U004",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "템플릿을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "블로그 템플릿을 찾을 수 없습니다. User ID: 1",
                                                "status": 404,
                                                "code": "BT001",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/me")
    public BlogTemplateResponse getMyTemplate(Principal principal) {
        Long userId = extractUserId(principal);
        return blogTemplateService.getTemplateResponseByUserId(userId, userId);
    }

    @Operation(summary = "특정 사용자의 블로그 템플릿 조회", description = "특정 사용자의 블로그 템플릿을 조회합니다. 본인의 템플릿이거나 관리자만 조회 가능합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BlogTemplateResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "인증이 필요합니다",
                                                "status": 401,
                                                "code": "U004",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (다른 사용자의 템플릿)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "본인의 템플릿만 조회할 수 있습니다",
                                                "status": 403,
                                                "code": "BT002",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "템플릿을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "블로그 템플릿을 찾을 수 없습니다. User ID: 999",
                                                "status": 404,
                                                "code": "BT001",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/user/{userId}")
    public BlogTemplateResponse getTemplateByUser(@PathVariable Long userId, Principal principal) {
        Long requesterId = extractUserId(principal);
        return blogTemplateService.getTemplateResponseByUserId(userId, requesterId);
    }

    @Operation(summary = "블로그 템플릿 검색", description = "카테고리 또는 플랫폼으로 블로그 템플릿을 검색합니다. (인증 필요)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BlogTemplateResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "인증이 필요합니다",
                                                "status": 401,
                                                "code": "U004",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/search")
    public List<BlogTemplateResponse> searchTemplates(
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> platforms
    ) {
        return blogTemplateService.searchTemplateResponses(categories, platforms);
    }

    @Operation(summary = "특정 시간대 블로그 템플릿 조회", description = "지정된 시간에 실행될 블로그 템플릿을 조회합니다. (스케줄러용, 인증 필요)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BlogTemplateResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 time 파라미터",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "유효하지 않은 시간 형식입니다",
                                                "status": 400,
                                                "code": "C001",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": [
                                                    {
                                                        "field": "time",
                                                        "message": "유효하지 않은 시간 형식입니다"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "인증이 필요합니다",
                                                "status": 401,
                                                "code": "U004",
                                                "timestamp": "2024-11-24T14:30:45.123456",
                                                "errors": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/schedule")
    public List<BlogTemplateResponse> getTemplatesForTime(@RequestParam("time") LocalTime postTime) {
        return blogTemplateService.getTemplateResponsesForTime(postTime);
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
