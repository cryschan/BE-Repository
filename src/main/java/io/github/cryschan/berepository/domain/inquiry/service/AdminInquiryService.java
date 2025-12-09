package io.github.cryschan.berepository.domain.inquiry.service;

import io.github.cryschan.berepository.domain.inquiry.dto.Request.CreateAnswerRequest;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.AdminInquiryListResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryDetailResponse;
import io.github.cryschan.berepository.domain.inquiry.entity.Answer.InquiryAnswer;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.Inquiry;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryStatus;
import io.github.cryschan.berepository.domain.inquiry.exception.InquiryAlreadyAnsweredException;
import io.github.cryschan.berepository.domain.inquiry.exception.InquiryAnswerNotFoundException;
import io.github.cryschan.berepository.domain.inquiry.repository.InquiryAnswerRepository;
import io.github.cryschan.berepository.domain.inquiry.repository.InquiryRepository;
import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import io.github.cryschan.berepository.domain.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 관리자 문의 Service
 *
 * ==================================================================
 * 💡 관리자 전용 기능
 * ==================================================================
 * 1. 모든 문의 조회
 * 2. 미답변 문의 목록 조회
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
     * 1. 모든 문의 조회 (관리자용) + 상태 필터링
     * ==================================================================
     *
     * 플로우:
     * STEP 1: 관리자 권한 확인
     * STEP 2: 상태에 따라 조회 (최신순)
     *         - status가 null이면 전체 조회
     *         - status가 있으면 해당 상태만 조회
     * STEP 3: Entity → DTO 변환 (사용자 정보 포함)
     */
    public Page<AdminInquiryListResponse> getAllInquiries(Long adminUserId, InquiryStatus status, int page, int size) {
        // STEP 1: 관리자 권한 확인
        validateAdmin(adminUserId);

        // 페이지 번호 검증
        if (page < 1) {
            page = 1;
        }

        // 페이지 크기 제한
        if (size > 100) {
            size = 100;
        }
        if (size < 1) {
            size = 10;
        }

        // 1-based → 0-based 변환
        int pageIndex = page - 1;

        // Pageable 생성 (최신순 정렬)
        Pageable pageable = PageRequest.of(
                pageIndex,
                size,
                Sort.by(Sort.Order.desc("createdAt"))
        );

        // STEP 2: 상태에 따라 문의 조회
        Page<Inquiry> inquiries;
        if (status == null) {
            // 전체 조회
            inquiries = inquiryRepository.findAll(pageable);
        } else {
            // 상태별 조회
            inquiries = inquiryRepository.findByStatus(status, pageable);
        }

        // STEP 3: Entity → DTO 변환 (사용자 정보 포함)
        return buildAdminInquiryPage(inquiries);
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
        return buildAdminInquiryList(inquiries);
    }

    /**
     * ==================================================================
     * 3. 관리자용 문의 상세 조회
     * ==================================================================
     *
     * 관리자는 모든 문의를 볼 수 있으므로 권한만 확인
     */
    public InquiryDetailResponse getInquiryDetail(Long adminUserId, Long inquiryId) {
        // 관리자 권한 확인
        validateAdmin(adminUserId);

        // 문의 조회
        Inquiry inquiry = inquiryRepository.getByIdOrThrow(inquiryId);

        // 문의 작성자의 userId로 상세 조회 (InquiryService 재사용)
        return inquiryService.getInquiryDetail(inquiry.getUserId(), inquiryId);
    }

    /**
     * ==================================================================
     * 4. 관리자 답변 작성 ⭐⭐⭐ 이미지의 "답변 등록" 버튼
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
        Inquiry inquiry = inquiryRepository.getByIdOrThrow(inquiryId);

        // STEP 3: 이미 답변이 있는지 확인
        if (inquiryAnswerRepository.existsByInquiry_Id(inquiryId)) {
            throw new InquiryAlreadyAnsweredException();
        }

        // STEP 4: InquiryAnswer 생성
        InquiryAnswer answer = InquiryAnswer.builder()
                .inquiry(inquiry)
                .adminUserId(adminUserId)
                .answerContent(request.getAnswerContent())
                .answeredAt(LocalDateTime.now())
                .build();

        // STEP 5: 저장 및 문의 상태 변경
        inquiryAnswerRepository.save(answer);
        inquiry.complete();

        // STEP 6: 응답 반환
        return inquiryService.getInquiryDetail(inquiry.getUserId(), inquiryId);
    }

    /**
     * ==================================================================
     * 5. 관리자 답변 삭제
     * ==================================================================
     *
     * 플로우:
     * STEP 1: 관리자 권한 확인
     * STEP 2: 문의 조회
     * STEP 3: 답변 조회
     * STEP 4: 답변 삭제 및 문의 상태를 PENDING으로 변경
     */
    @Transactional
    public void deleteAnswer(Long adminUserId, Long inquiryId) {
        // STEP 1: 관리자 권한 확인
        validateAdmin(adminUserId);

        // STEP 2: 문의 조회
        Inquiry inquiry = inquiryRepository.getByIdOrThrow(inquiryId);

        // STEP 3: 답변 조회
        InquiryAnswer answer = inquiryAnswerRepository.findByInquiry_Id(inquiryId)
                .orElseThrow(() -> new InquiryAnswerNotFoundException(inquiryId));

        // STEP 4: 답변 삭제 및 문의 상태를 PENDING으로 변경
        inquiryAnswerRepository.delete(answer);
        inquiry.reopen();
    }

    /**
     * ==================================================================
     * 6. 문의 삭제 (관리자 전용) ⭐
     * ==================================================================
     *
     * 플로우:
     * STEP 1: 관리자 권한 확인
     * STEP 2: 문의 조회
     * STEP 3: 답변이 있으면 먼저 삭제
     * STEP 4: 문의 삭제
     */
    @Transactional
    public void deleteInquiry(Long adminUserId, Long inquiryId) {
        // STEP 1: 관리자 권한 확인
        validateAdmin(adminUserId);

        // STEP 2: 문의 조회
        Inquiry inquiry = inquiryRepository.getByIdOrThrow(inquiryId);

        // STEP 3: 답변이 있으면 먼저 삭제 (FK 제약조건 때문에)
        inquiryAnswerRepository.findByInquiry_Id(inquiryId)
                .ifPresent(inquiryAnswerRepository::delete);

        // STEP 4: 문의 삭제
        inquiryRepository.delete(inquiry);
    }


    /**
     * 관리자 권한 검증
     *
     * @param adminUserId 검증할 사용자 ID
     * @return 검증된 관리자 User 엔티티
     * @throws UserException 사용자가 없거나 관리자가 아닌 경우
     */
    private User validateAdmin(Long adminUserId) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> UserException.notFound(adminUserId));

        if (admin.getRole() != UserRole.ADMIN) {
            throw UserException.accessDenied("문의 관리 기능");
        }

        return admin;
    }

    private Page<AdminInquiryListResponse> buildAdminInquiryPage(Page<Inquiry> inquiries) {
        List<AdminInquiryListResponse> content = buildAdminInquiryList(inquiries.getContent());
        return new PageImpl<>(content, inquiries.getPageable(), inquiries.getTotalElements());
    }

    private List<AdminInquiryListResponse> buildAdminInquiryList(List<Inquiry> inquiries) {
        if (inquiries.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, User> userMap = getUserMap(inquiries);
        Set<Long> answeredInquiryIds = getAnsweredInquiryIds(inquiries);

        return inquiries.stream()
                .map(inquiry -> convertToAdminListResponse(
                        inquiry,
                        userMap.get(inquiry.getUserId()),
                        answeredInquiryIds.contains(inquiry.getId())
                ))
                .collect(Collectors.toList());
    }

    private Map<Long, User> getUserMap(List<Inquiry> inquiries) {
        List<Long> userIds = inquiries.stream()
                .map(Inquiry::getUserId)
                .distinct()
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));
    }

    private Set<Long> getAnsweredInquiryIds(List<Inquiry> inquiries) {
        List<Long> inquiryIds = inquiries.stream()
                .map(Inquiry::getId)
                .collect(Collectors.toList());

        if (inquiryIds.isEmpty()) {
            return Collections.emptySet();
        }

        return new HashSet<>(inquiryAnswerRepository.findAnsweredInquiryIds(inquiryIds));
    }

    private AdminInquiryListResponse convertToAdminListResponse(Inquiry inquiry, User user, boolean hasAnswer) {
        if (user == null) {
            throw UserException.notFound(inquiry.getUserId());
        }

        return AdminInquiryListResponse.builder()
                .id(inquiry.getId())
                .userId(inquiry.getUserId())
                .userEmail(user.getEmail())
                .userName(user.getUsername())
                .title(inquiry.getTitle())
                .inquiryCategory(inquiry.getInquiryCategory())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .hasAnswer(hasAnswer)
                .build();
    }
}
