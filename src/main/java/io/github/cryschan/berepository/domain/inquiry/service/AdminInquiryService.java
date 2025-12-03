package io.github.cryschan.berepository.domain.inquiry.service;

import io.github.cryschan.berepository.domain.inquiry.dto.Request.CreateAnswerRequest;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.AdminInquiryListResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryDetailResponse;
import io.github.cryschan.berepository.domain.inquiry.entity.Answer.InquiryAnswer;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.Inquiry;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryStatus;
import io.github.cryschan.berepository.domain.inquiry.exception.InquiryAlreadyAnsweredException;
import io.github.cryschan.berepository.domain.inquiry.exception.InquiryNotFoundException;
import io.github.cryschan.berepository.domain.inquiry.repository.InquiryAnswerRepository;
import io.github.cryschan.berepository.domain.inquiry.repository.InquiryRepository;
import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 관리자 문의 Service
 *
 * ==================================================================
 * 💡 관리자 전용 기능
 * ==================================================================
 * 1. 모든 문의 조회
 * 2. 미답변 문의 건수 조회
 * 3. 답변 작성
 *
 * ⚠️ 모든 메서드는 관리자 권한 확인 필요!
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final UserRepository userRepository;
    private final InquiryService inquiryService; // 변환 메서드 재사용

    /**
     * ==================================================================
     * 1. 모든 문의 조회 (관리자용)
     * ==================================================================
     *
     * 플로우:
     * STEP 1: 관리자 권한 확인
     * STEP 2: 모든 문의 조회 (최신순)
     * STEP 3: Entity → DTO 변환 (사용자 정보 포함)
     */
    public Page<AdminInquiryListResponse> getAllInquiries(Long adminUserId, Pageable pageable) {
        // STEP 1: 관리자 권한 확인
        validateAdmin(adminUserId);

        // STEP 2: 모든 문의 조회 (최신순)
        Page<Inquiry> inquiries = inquiryRepository.findAll(pageable);

        // STEP 3: Entity → DTO 변환 (사용자 정보 포함)
        return inquiries.map(this::convertToAdminListResponse);
    }

    /**
     * ==================================================================
     * 2. 미답변 문의 목록 조회
     * ==================================================================
     *
     * 플로우:
     * STEP 1: 관리자 권한 확인
     * STEP 2: PENDING 상태 문의 조회
     * STEP 3: DTO 변환
     */
    public List<AdminInquiryListResponse> getPendingInquiries(Long adminUserId) {
        // STEP 1: 관리자 권한 확인
        validateAdmin(adminUserId);

        // STEP 2: PENDING 상태 문의 조회
        List<Inquiry> inquiries = inquiryRepository.findByStatusOrderByCreatedAtDesc(InquiryStatus.PENDING);

        // STEP 3: DTO 변환
        return inquiries.stream()
                .map(this::convertToAdminListResponse)
                .collect(Collectors.toList());
    }

    /**
     * ==================================================================
     * 3. 미답변 문의 건수 조회 ⭐ 이미지의 "미답변 문의가 3건이 표시됩니다"
     * ==================================================================
     *
     * 플로우:
     * STEP 1: 관리자 권한 확인
     * STEP 2: PENDING 상태 문의 개수 반환
     */
    public long getPendingInquiriesCount(Long adminUserId) {
        // STEP 1: 관리자 권한 확인
        validateAdmin(adminUserId);

        // STEP 2: PENDING 상태 문의 개수 반환
        return inquiryRepository.findByStatusOrderByCreatedAtDesc(InquiryStatus.PENDING).size();
    }

    /**
     * ==================================================================
     * 4. 관리자용 문의 상세 조회
     * ==================================================================
     *
     * 관리자는 모든 문의를 볼 수 있으므로 권한만 확인
     */
    public InquiryDetailResponse getInquiryDetail(Long adminUserId, Long inquiryId) {
        // 관리자 권한 확인
        validateAdmin(adminUserId);

        // 문의 조회
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryNotFoundException(inquiryId));

        // 문의 작성자의 userId로 상세 조회 (InquiryService 재사용)
        return inquiryService.getInquiryDetail(inquiry.getUserId(), inquiryId);
    }

    /**
     * ==================================================================
     * 5. 관리자 답변 작성 ⭐⭐⭐ 이미지의 "답변 등록" 버튼
     * ==================================================================
     *
     * 플로우:
     * STEP 1: 관리자 권한 확인
     * STEP 2: 문의 조회
     * STEP 3: 이미 답변이 있는지 확인
     * STEP 4: InquiryAnswer 생성
     * STEP 5: 저장 및 문의 상태 변경
     * STEP 6: 응답 반환
     */
    @Transactional
    public InquiryDetailResponse answerInquiry(
            Long adminUserId,
            Long inquiryId,
            CreateAnswerRequest request
    ) {
        // STEP 1: 관리자 권한 확인
        validateAdmin(adminUserId);

        // STEP 2: 문의 조회
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new InquiryNotFoundException(inquiryId));

        // STEP 3: 이미 답변이 있는지 확인
        if (inquiryAnswerRepository.existsByInquiryId(inquiryId)) {
            throw new InquiryAlreadyAnsweredException();
        }

        // STEP 4: InquiryAnswer 생성
        InquiryAnswer answer = InquiryAnswer.builder()
                .inquiryId(inquiryId)
                .adminUserId(adminUserId)
                .answerContent(request.getAnswerContent())
                .build();

        // STEP 5: 저장 및 문의 상태 변경
        inquiryAnswerRepository.save(answer);
        inquiry.complete();

        // STEP 6: 응답 반환
        return inquiryService.getInquiryDetail(inquiry.getUserId(), inquiryId);
    }


    /**
     * 관리자 권한 검증
     *
     * @param adminUserId 검증할 사용자 ID
     * @return 검증된 관리자 User 엔티티
     * @throws RuntimeException 사용자가 없거나 관리자가 아닌 경우
     */
    private User validateAdmin(Long adminUserId) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("관리자만 접근할 수 있습니다.");
        }

        return admin;
    }

    /**
     * Entity → DTO 변환 (사용자 정보 포함)
     */
    private AdminInquiryListResponse convertToAdminListResponse(Inquiry inquiry) {
        // 사용자 정보 조회
        User user = userRepository.findById(inquiry.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 답변 존재 여부 확인
        boolean hasAnswer = inquiryAnswerRepository.existsByInquiryId(inquiry.getId());

        // AdminInquiryListResponse 생성
        return AdminInquiryListResponse.builder()
                .id(inquiry.getId())
                .userId(inquiry.getUserId())
                .userEmail(user.getEmail())
                .userName(user.getUsername())
                .title(inquiry.getTitle())
                .inquiryCategory(inquiry.getInquiryCategory())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .hasAnswer(hasAnswer)
                .build();
    }
}
