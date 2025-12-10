package io.github.cryschan.berepository.domain.inquiry.controller;

import io.github.cryschan.berepository._global.exception.dto.ErrorResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Request.CreateAnswerRequest;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.AdminInquiryListResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.AdminInquiryPageResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryDetailResponse;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryStatus;
import io.github.cryschan.berepository.domain.inquiry.service.AdminInquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자 문의 Controller
 * <p>
 * ==================================================================
 * 🖼️ 이미지 매핑
 * ==================================================================
 * [Image #2] - 관리자 문의 목록 페이지
 * - GET /api/admin/inquiries - 전체 문의 목록
 * - GET /api/admin/inquiries/pending - 미답변 문의 목록
 * <p>
 * [Image #1] - 답변 작성 모달
 * - POST /api/admin/inquiries/{id}/answer - 답변 등록
 * <p>
 * ==================================================================
 * API 엔드포인트
 * ==================================================================
 * GET    /api/admin/inquiries                 - 전체 문의 목록 (페이징)
 * GET    /api/admin/inquiries/pending         - 미답변 문의 목록
 * GET    /api/admin/inquiries/{id}            - 문의 상세
 * POST   /api/admin/inquiries/{id}/answer     - 답변 작성 ⭐
 * DELETE /api/admin/inquiries/{id}/answer     - 답변 삭제
 * DELETE /api/admin/inquiries/{id}            - 문의 삭제 ⭐⭐
 */
@Tag(name = "Admin Inquiry", description = "관리자 문의 관리 API")
@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final AdminInquiryService adminInquiryService;

    /**
     * 1. 전체 문의 목록 조회 (페이징) + 상태 필터링
     * <p>
     * 플로우:
     * 1) 현재 로그인한 관리자 ID 가져오기
     * 2) status 파라미터로 필터링 (선택)
     * 3) Service 호출
     * 4) 페이징된 결과 반환
     * <p>
     * Query Parameters:
     * - status: PENDING (미답변) / COMPLETED (답변완료) / IN_PROGRESS (답변중) / null (전체)
     */
    @Operation(
            summary = "전체 문의 목록 조회",
            description = """
                    관리자가 모든 사용자의 문의를 조회합니다.
                    
                    **기능:**
                    - 페이징 지원 (page, size 파라미터)
                    - 상태별 필터링 (status 파라미터)
                    - 최신순 정렬
                    
                    **Query Parameters:**
                    - `status`: null(전체) / PENDING (미답변) / IN_PROGRESS (답변중) / COMPLETED (답변완료)
                    - `page`: 페이지 번호 (1부터 시작)
                    - `size`: 페이지 크기
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "문의 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdminInquiryPageResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "inquiries": [
                                        {
                                          "id": 1,
                                          "userId": 123,
                                          "userEmail": "user1@example.com",
                                          "userName": "김민수",
                                          "title": "로그인 문제 문의",
                                          "inquiryCategory": "ACCOUNT",
                                          "status": "COMPLETED",
                                          "createdAt": "2025-12-08T10:30:00",
                                          "updatedAt": "2025-12-08T15:20:00",
                                          "hasAnswer": true
                                        },
                                        {
                                          "id": 2,
                                          "userId": 124,
                                          "userEmail": "user2@example.com",
                                          "userName": "이영희",
                                          "title": "결제 오류 문의",
                                          "inquiryCategory": "PAYMENT",
                                          "status": "PENDING",
                                          "createdAt": "2025-12-09T09:15:00",
                                          "updatedAt": "2025-12-09T09:15:00",
                                          "hasAnswer": false
                                        }
                                      ],
                                      "currentPage": 1,
                                      "totalPages": 3,
                                      "totalElements": 25,
                                      "size": 10,
                                      "first": true,
                                      "last": false
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인 필요",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "인증이 필요합니다",
                                      "status": 401,
                                      "code": "U004",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 관리자만 접근 가능",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "접근 권한이 없습니다",
                                      "status": 403,
                                      "code": "U005",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            )
    })
    @GetMapping
    public ResponseEntity<AdminInquiryPageResponse> getAllInquiries(
            @AuthenticationPrincipal Long adminUserId,
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        Page<AdminInquiryListResponse> inquiries = adminInquiryService.getAllInquiries(adminUserId, status, page, size);
        return ResponseEntity.ok(AdminInquiryPageResponse.from(inquiries));
    }

    /**
     * 2. 미답변 문의 목록 조회
     * <p>
     * 🖼️ Image #2의 테이블 데이터
     * 답변 상태 : PENDING, COMPLETED
     */
    @Operation(
            summary = "미답변 문의 목록 조회",
            description = """
                    답변이 등록되지 않은 문의(PENDING 상태)만 조회합니다.
                    
                    **기능:**
                    - PENDING 상태의 문의만 필터링
                    - 최신순 정렬
                    - 페이징 없이 전체 목록 반환
                    
                    **사용 시나리오:**
                    - 관리자 대시보드에서 처리 필요한 문의 확인
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "미답변 문의 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdminInquiryListResponse.class),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "id": 1,
                                        "userId": 123,
                                        "userEmail": "user1@example.com",
                                        "userName": "김민수",
                                        "title": "로그인 문제 문의",
                                        "inquiryCategory": "ACCOUNT",
                                        "status": "PENDING",
                                        "createdAt": "2025-12-09T09:15:00",
                                        "updatedAt": "2025-12-09T09:15:00",
                                        "hasAnswer": false
                                      },
                                      {
                                        "id": 2,
                                        "userId": 124,
                                        "userEmail": "user2@example.com",
                                        "userName": "이영희",
                                        "title": "결제 오류 문의",
                                        "inquiryCategory": "PAYMENT",
                                        "status": "PENDING",
                                        "createdAt": "2025-12-09T08:30:00",
                                        "updatedAt": "2025-12-09T08:30:00",
                                        "hasAnswer": false
                                      }
                                    ]
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "인증이 필요합니다",
                                      "status": 401,
                                      "code": "U004",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 관리자만 접근 가능",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "접근 권한이 없습니다",
                                      "status": 403,
                                      "code": "U005",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            )
    })
    @GetMapping("/pending")
    public ResponseEntity<List<AdminInquiryListResponse>> getPendingInquiries(
            @AuthenticationPrincipal Long adminUserId
    ) {
        List<AdminInquiryListResponse> inquiries = adminInquiryService.getPendingInquiries(adminUserId);
        return ResponseEntity.ok(inquiries);
    }

    /**
     * 3. 관리자 문의 상세 조회
     * <p>
     * 관리자는 모든 문의를 볼 수 있으므로, Service에서 권한 확인 후
     * 문의를 작성한 사용자 ID로 InquiryService.getInquiryDetail() 호출
     */
    @Operation(
            summary = "문의 상세 조회 (관리자)",
            description = """
                    관리자가 특정 문의의 상세 정보를 조회합니다.

                    **기능:**
                    - 모든 사용자의 문의 조회 가능 (소유권 검증 없음)
                    - 문의 내용 + 답변 내용 포함

                    **Path Parameter:**
                    - `id`: 조회할 문의 ID

                    **사용 시나리오:**
                    - 문의 목록에서 특정 문의 클릭 시
                    - 답변 작성 전 문의 내용 확인
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "문의 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InquiryDetailResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "userId": 123,
                                      "title": "로그인 문제 문의",
                                      "inquiryCategory": "ACCOUNT",
                                      "content": "로그인이 되지 않습니다. 도움이 필요합니다.",
                                      "status": "COMPLETED",
                                      "createdAt": "2025-12-08T10:30:00",
                                      "updatedAt": "2025-12-08T15:20:00",
                                      "answer": {
                                        "id": 1,
                                        "adminUserId": 1,
                                        "answerContent": "비밀번호 재설정 링크를 이메일로 보내드렸습니다. 확인 부탁드립니다.",
                                        "answeredAt": "2025-12-08T15:20:00"
                                      }
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "인증이 필요합니다",
                                      "status": 401,
                                      "code": "U004",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 관리자만 접근 가능",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "접근 권한이 없습니다",
                                      "status": 403,
                                      "code": "U005",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "문의를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "문의를 찾을 수 없습니다",
                                      "status": 404,
                                      "code": "I001",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<InquiryDetailResponse> getInquiryDetail(
            @AuthenticationPrincipal Long adminUserId,
            @PathVariable Long id
    ) {
        InquiryDetailResponse response = adminInquiryService.getInquiryDetail(adminUserId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * 4. 답변 작성 ⭐⭐⭐
     * <p>
     * 🖼️ Image #1의 "답변 등록" 버튼
     * <p>
     * Request Body:
     * {
     * "answerContent": "응대해 대한 답변을 작성하세요"
     * }
     * <p>
     * Response:
     * - 201 Created
     * - Body: InquiryDetailResponse (답변 포함)
     */
    @Operation(
            summary = "문의에 답변 작성",
            description = """
                    관리자가 특정 문의에 답변을 등록합니다.
                    
                    **기능:**
                    - 답변 내용 저장
                    - 문의 상태를 PENDING → COMPLETED로 변경
                    - 답변 등록 시간 자동 기록
                    
                    **Request Body:**
                    ```json
                    {
                      "answerContent": "문의에 대한 답변 내용을 작성하세요"
                    }
                    ```
                    
                    **Path Parameter:**
                    - `id`: 답변을 작성할 문의 ID
                    
                    **사용 시나리오:**
                    - 문의 상세 페이지에서 "답변 등록" 버튼 클릭
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "답변 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InquiryDetailResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "userId": 123,
                                      "title": "로그인 문제 문의",
                                      "inquiryCategory": "ACCOUNT",
                                      "content": "로그인이 되지 않습니다. 도움이 필요합니다.",
                                      "status": "COMPLETED",
                                      "createdAt": "2025-12-08T10:30:00",
                                      "updatedAt": "2025-12-09T14:30:00",
                                      "answer": {
                                        "id": 1,
                                        "adminUserId": 1,
                                        "answerContent": "비밀번호 재설정 링크를 이메일로 보내드렸습니다. 확인 부탁드립니다.",
                                        "answeredAt": "2025-12-09T14:30:00"
                                      }
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "이미 답변이 등록된 문의",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "이미 답변이 등록된 문의입니다",
                                      "status": 400,
                                      "code": "I003",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "인증이 필요합니다",
                                      "status": 401,
                                      "code": "U004",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 관리자만 접근 가능",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "접근 권한이 없습니다",
                                      "status": 403,
                                      "code": "U005",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "문의를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "문의를 찾을 수 없습니다",
                                      "status": 404,
                                      "code": "I001",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            )
    })
    @PostMapping("/{id}/answer")
    public ResponseEntity<InquiryDetailResponse> answerInquiry(
            @AuthenticationPrincipal Long adminUserId,
            @PathVariable Long id,
            @RequestBody CreateAnswerRequest request
    ) {
        InquiryDetailResponse response = adminInquiryService.answerInquiry(adminUserId, id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 5. 답변 삭제
     * <p>
     * 플로우:
     * 1) 현재 로그인한 관리자 ID 가져오기
     * 2) Service 호출하여 답변 삭제
     * 3) 문의 상태 PENDING으로 변경
     * <p>
     * Response:
     * - 204 No Content
     */
    @Operation(
            summary = "답변 삭제",
            description = """
                    관리자가 작성한 답변을 삭제합니다.

                    **기능:**
                    - 답변 데이터 삭제
                    - 문의 상태를 COMPLETED → PENDING으로 변경
                    - 재답변 가능 상태로 전환

                    **Path Parameter:**
                    - `id`: 답변을 삭제할 문의 ID

                    **사용 시나리오:**
                    - 잘못 작성된 답변 삭제 후 재작성
                    - 답변 수정이 필요한 경우 (삭제 후 재등록)
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "답변 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "인증이 필요합니다",
                                      "status": 401,
                                      "code": "U004",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 관리자만 접근 가능",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "접근 권한이 없습니다",
                                      "status": 403,
                                      "code": "U005",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "문의 또는 답변을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "문의에 대한 답변을 찾을 수 없습니다",
                                      "status": 404,
                                      "code": "I004",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            )
    })
    @DeleteMapping("/{id}/answer")
    public ResponseEntity<Void> deleteAnswer(
            @AuthenticationPrincipal Long adminUserId,
            @PathVariable Long id
    ) {
        adminInquiryService.deleteAnswer(adminUserId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 6. 문의 삭제 ⭐⭐
     * <p>
     * 플로우:
     * 1) 현재 로그인한 관리자 ID 가져오기
     * 2) Service 호출하여 문의 삭제
     * 3) 답변이 있으면 함께 삭제 (CASCADE)
     * <p>
     * Response:
     * - 204 No Content
     */
    @Operation(
            summary = "문의 삭제 (관리자 전용)",
            description = """
                    관리자가 특정 문의를 완전히 삭제합니다.

                    **기능:**
                    - 문의 데이터 완전 삭제
                    - 연결된 답변도 함께 삭제 (CASCADE)
                    - 복구 불가능한 작업

                    **Path Parameter:**
                    - `id`: 삭제할 문의 ID

                    **사용 시나리오:**
                    - 스팸/악의적 문의 삭제
                    - 중복 문의 정리
                    - 테스트 데이터 삭제

                    **⚠️ 주의:**
                    - 이 작업은 되돌릴 수 없습니다
                    - 답변이 있는 경우에도 강제 삭제됩니다
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "문의 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "인증이 필요합니다",
                                      "status": 401,
                                      "code": "U004",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 관리자만 접근 가능",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "접근 권한이 없습니다",
                                      "status": 403,
                                      "code": "U005",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "문의를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "문의를 찾을 수 없습니다",
                                      "status": 404,
                                      "code": "I001",
                                      "timestamp": "2025-12-09T12:34:56"
                                    }
                                    """))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInquiry(
            @AuthenticationPrincipal Long adminUserId,
            @PathVariable Long id
    ) {
        adminInquiryService.deleteInquiry(adminUserId, id);
        return ResponseEntity.noContent().build();
    }
}
