package io.github.cryschan.berepository.domain.inquiry.controller;

import io.github.cryschan.berepository.domain.inquiry.dto.Request.CreateAnswerRequest;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.AdminInquiryListResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryDetailResponse;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryStatus;
import io.github.cryschan.berepository.domain.inquiry.service.AdminInquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 관리자 문의 Controller
 *
 * ==================================================================
 * 🖼️ 이미지 매핑
 * ==================================================================
 * [Image #2] - 관리자 문의 목록 페이지
 *   - GET /api/admin/inquiries - 전체 문의 목록
 *   - GET /api/admin/inquiries/pending - 미답변 문의 목록
 *   - GET /api/admin/inquiries/pending/count - 미답변 건수 (3건)
 *
 * [Image #1] - 답변 작성 모달
 *   - POST /api/admin/inquiries/{id}/answer - 답변 등록
 *
 * ==================================================================
 * API 엔드포인트
 * ==================================================================
 * GET    /api/admin/inquiries                 - 전체 문의 목록 (페이징)
 * GET    /api/admin/inquiries/pending         - 미답변 문의 목록
 * GET    /api/admin/inquiries/pending/count   - 미답변 문의 건수
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
     *
     * 플로우:
     * 1) 현재 로그인한 관리자 ID 가져오기
     * 2) status 파라미터로 필터링 (선택)
     * 3) Service 호출
     * 4) 페이징된 결과 반환
     *
     * Query Parameters:
     * - status: PENDING (미답변) / COMPLETED (답변완료) / null (전체)
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
                    - `status`: PENDING (미답변) / COMPLETED (답변완료) / null (전체)
                    - `page`: 페이지 번호 (0부터 시작)
                    - `size`: 페이지 크기
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "문의 목록 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인 필요"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 관리자만 접근 가능"
            )
    })
    @GetMapping
    public ResponseEntity<Page<AdminInquiryListResponse>> getAllInquiries(
            @AuthenticationPrincipal Long adminUserId,
            @RequestParam(required = false) InquiryStatus status,
            Pageable pageable
    ) {
        Page<AdminInquiryListResponse> inquiries = adminInquiryService.getAllInquiries(adminUserId, status, pageable);
        return ResponseEntity.ok(inquiries);
    }

    /**
     * 2. 미답변 문의 목록 조회
     *
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
                    description = "미답변 문의 목록 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패"
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
     * 3. 미답변 문의 건수 조회 ⭐
     *
     * 🖼️ Image #2의 "미답변 문의가 3건이 표시됩니다"
     *
     * Response 형식: { "count": 3 }
     */
    @Operation(
            summary = "미답변 문의 건수 조회",
            description = """
                    답변이 필요한 문의(PENDING 상태)의 총 개수를 반환합니다.

                    **응답 형식:**
                    ```json
                    {
                      "count": 3
                    }
                    ```

                    **사용 시나리오:**
                    - 관리자 대시보드에 미답변 문의 배지 표시
                    - 처리 필요한 문의 알림
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "미답변 문의 건수 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패"
            )
    })
    @GetMapping("/pending/count")
    public ResponseEntity<Map<String, Long>> getPendingInquiriesCount(
            @AuthenticationPrincipal Long adminUserId
    ) {
        long count = adminInquiryService.getPendingInquiriesCount(adminUserId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 4. 관리자 문의 상세 조회
     *
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
                    description = "문의 상세 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "문의를 찾을 수 없음"
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
     * 5. 답변 작성 ⭐⭐⭐
     *
     * 🖼️ Image #1의 "답변 등록" 버튼
     *
     * Request Body:
     * {
     *   "answerContent": "응대해 대한 답변을 작성하세요"
     * }
     *
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
                    description = "답변 등록 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "이미 답변이 등록된 문의"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "문의를 찾을 수 없음"
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
     * 6. 답변 삭제
     *
     * 플로우:
     * 1) 현재 로그인한 관리자 ID 가져오기
     * 2) Service 호출하여 답변 삭제
     * 3) 문의 상태 PENDING으로 변경
     *
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
                    description = "인증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "문의 또는 답변을 찾을 수 없음"
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
     * 7. 문의 삭제 ⭐⭐
     *
     * 플로우:
     * 1) 현재 로그인한 관리자 ID 가져오기
     * 2) Service 호출하여 문의 삭제
     * 3) 답변이 있으면 함께 삭제 (CASCADE)
     *
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
                    description = "인증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 관리자만 접근 가능"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "문의를 찾을 수 없음"
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
