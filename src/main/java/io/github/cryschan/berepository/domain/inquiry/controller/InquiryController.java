package io.github.cryschan.berepository.domain.inquiry.controller;

import io.github.cryschan.berepository.domain.inquiry.dto.Request.CreateInquiryRequest;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryDetailResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryListResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryPageResponse;
import io.github.cryschan.berepository.domain.inquiry.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 문의 Controller
 *
 * API 엔드포인트:
 * POST   /api/inquiries          - 문의 생성
 * GET    /api/inquiries          - 내 문의 목록
 * GET    /api/inquiries/{id}     - 문의 상세
 */
@Tag(name = "Inquiry", description = "사용자 문의 API")
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    /**
     * 1. 문의 생성
     *
     * 플로우:
     * 1) 사용자가 문의 작성 폼 제출
     * 2) Request Body에서 CreateInquiryRequest 받기
     * 3) 현재 로그인한 사용자 ID 가져오기 (SecurityContext 또는 @AuthenticationPrincipal)
     * 4) InquiryService.createInquiry() 호출
     * 5) 생성된 문의 정보 반환 (201 Created)
     *
     * @param userId 현재 로그인한 사용자 ID
     * @param request 문의 생성 요청 DTO
     * @return 생성된 문의 상세 정보
     */
    @Operation(summary = "문의 생성", description = "새로운 문의를 생성합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "문의 생성 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquiryDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public ResponseEntity<InquiryDetailResponse> createInquiry(
            @AuthenticationPrincipal Long userId,
            @RequestBody CreateInquiryRequest request
    ) {
        InquiryDetailResponse response = inquiryService.createInquiry(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 2. 내 문의 목록 조회 (페이징)
     *
     * 플로우:
     * 1) 사용자가 "내 문의 목록" 페이지 접속
     * 2) 현재 로그인한 사용자 ID 가져오기
     * 3) InquiryService.getMyInquiries() 호출
     * 4) 페이징된 문의 목록 반환 (200 OK)
     *
     * @param userId 현재 로그인한 사용자 ID
     * @param page 페이지 번호 (1부터 시작)
     * @return 페이징된 사용자의 문의 목록
     */
    @Operation(
            summary = "내 문의 목록 조회",
            description = "현재 로그인한 사용자의 문의 목록을 페이지네이션하여 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "문의 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InquiryPageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping
    public ResponseEntity<InquiryPageResponse> getMyInquiries(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") int page
    ) {
        Page<InquiryListResponse> inquiries = inquiryService.getMyInquiries(userId, page);
        return ResponseEntity.ok(InquiryPageResponse.from(inquiries));
    }

    /**
     * 3. 문의 상세 조회
     *
     * 플로우:
     * 1) 사용자가 특정 문의 클릭
     * 2) Path Variable에서 inquiryId 받기
     * 3) 현재 로그인한 사용자 ID 가져오기
     * 4) InquiryService.getInquiryDetail() 호출
     *    - Service에서 본인 문의인지 확인
     * 5) 문의 상세 정보 반환 (200 OK)
     *
     * @param userId 현재 로그인한 사용자 ID
     * @param id 조회할 문의 ID
     * @return 문의 상세 정보 (답변 포함)
     */
    @Operation(summary = "문의 상세 조회", description = "특정 문의의 상세 정보를 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문의 상세 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquiryDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인의 문의가 아님)", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "문의를 찾을 수 없음", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{id}")
    public ResponseEntity<InquiryDetailResponse> getInquiryDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        InquiryDetailResponse response = inquiryService.getInquiryDetail(userId, id);
        return ResponseEntity.ok(response);
    }

}
