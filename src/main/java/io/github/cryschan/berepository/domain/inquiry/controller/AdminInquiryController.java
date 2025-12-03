package io.github.cryschan.berepository.domain.inquiry.controller;

import io.github.cryschan.berepository.domain.inquiry.dto.Request.CreateAnswerRequest;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.AdminInquiryListResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryDetailResponse;
import io.github.cryschan.berepository.domain.inquiry.service.AdminInquiryService;
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
 */
@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final AdminInquiryService adminInquiryService;

    /**
     * 1. 전체 문의 목록 조회 (페이징)
     *
     * 플로우:
     * 1) 현재 로그인한 관리자 ID 가져오기
     * 2) Service 호출
     * 3) 페이징된 결과 반환
     */
    @GetMapping
    public ResponseEntity<Page<AdminInquiryListResponse>> getAllInquiries(
            @AuthenticationPrincipal Long adminUserId,
            Pageable pageable
    ) {
        Page<AdminInquiryListResponse> inquiries = adminInquiryService.getAllInquiries(adminUserId, pageable);
        return ResponseEntity.ok(inquiries);
    }

    /**
     * 2. 미답변 문의 목록 조회
     *
     * 🖼️ Image #2의 테이블 데이터
     * 답변 상태 : PENDING, COMPLETED
     */
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
    @PostMapping("/{id}/answer")
    public ResponseEntity<InquiryDetailResponse> answerInquiry(
            @AuthenticationPrincipal Long adminUserId,
            @PathVariable Long id,
            @RequestBody CreateAnswerRequest request
    ) {
        InquiryDetailResponse response = adminInquiryService.answerInquiry(adminUserId, id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
