package io.github.cryschan.berepository.domain.inquiry.controller;

import io.github.cryschan.berepository.domain.inquiry.dto.Request.CreateInquiryRequest;
import io.github.cryschan.berepository.domain.inquiry.dto.Request.UpdateInquiryRequest;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryDetailResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryListResponse;
import io.github.cryschan.berepository.domain.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
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
 * PUT    /api/inquiries/{id}     - 문의 수정
 * DELETE /api/inquiries/{id}     - 문의 삭제
 */
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
    @PostMapping
    public ResponseEntity<InquiryDetailResponse> createInquiry(
            @AuthenticationPrincipal Long userId,
            @RequestBody CreateInquiryRequest request
    ) {
        InquiryDetailResponse response = inquiryService.createInquiry(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 2. 내 문의 목록 조회
     *
     * 플로우:
     * 1) 사용자가 "내 문의 목록" 페이지 접속
     * 2) 현재 로그인한 사용자 ID 가져오기
     * 3) InquiryService.getMyInquiries() 호출
     * 4) 문의 목록 반환 (200 OK)
     *
     * @param userId 현재 로그인한 사용자 ID
     * @return 사용자의 문의 목록
     */
    @GetMapping
    public ResponseEntity<List<InquiryListResponse>> getMyInquiries(
            @AuthenticationPrincipal Long userId
    ) {
        List<InquiryListResponse> inquiries = inquiryService.getMyInquiries(userId);
        return ResponseEntity.ok(inquiries);
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
    @GetMapping("/{id}")
    public ResponseEntity<InquiryDetailResponse> getInquiryDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        InquiryDetailResponse response = inquiryService.getInquiryDetail(userId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * 4. 문의 수정
     *
     * 플로우:
     * 1) 사용자가 자신의 문의 수정
     * 2) Path Variable에서 inquiryId, Request Body에서 UpdateInquiryRequest 받기
     * 3) 현재 로그인한 사용자 ID 가져오기
     * 4) InquiryService.updateInquiry() 호출
     *    - Service에서 본인 문의인지, 답변 달렸는지 확인
     * 5) 수정된 문의 정보 반환 (200 OK)
     *
     * @param userId 현재 로그인한 사용자 ID
     * @param id 수정할 문의 ID
     * @param request 문의 수정 요청 DTO
     * @return 수정된 문의 상세 정보
     */
    @PutMapping("/{id}")
    public ResponseEntity<InquiryDetailResponse> updateInquiry(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody UpdateInquiryRequest request
    ) {
        InquiryDetailResponse response = inquiryService.updateInquiry(userId, id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 5. 문의 삭제
     *
     * 플로우:
     * 1) 사용자가 자신의 문의 삭제
     * 2) Path Variable에서 inquiryId 받기
     * 3) 현재 로그인한 사용자 ID 가져오기
     * 4) InquiryService.deleteInquiry() 호출
     *    - Service에서 본인 문의인지 확인
     * 5) 204 No Content 반환
     *
     * @param userId 현재 로그인한 사용자 ID
     * @param id 삭제할 문의 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInquiry(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        inquiryService.deleteInquiry(userId, id);
        return ResponseEntity.noContent().build();
    }
}
