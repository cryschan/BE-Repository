package io.github.cryschan.berepository.domain.inquiry.service;

import io.github.cryschan.berepository.domain.inquiry.dto.Request.CreateInquiryRequest;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryAnswerResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryDetailResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryListResponse;
import io.github.cryschan.berepository.domain.inquiry.entity.Answer.InquiryAnswer;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.Inquiry;
import io.github.cryschan.berepository.domain.inquiry.exception.InvalidInquiryPageException;
import io.github.cryschan.berepository.domain.inquiry.exception.InquiryAlreadyAnsweredException;
import io.github.cryschan.berepository.domain.inquiry.exception.InquiryNotFoundException;
import io.github.cryschan.berepository.domain.inquiry.exception.UnauthorizedInquiryAccessException;
import io.github.cryschan.berepository.domain.inquiry.repository.InquiryAnswerRepository;
import io.github.cryschan.berepository.domain.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 사용자 문의 Service
 *
 * ==================================================================
 * 💡 Service Layer의 역할
 * ==================================================================
 * 1. 비즈니스 로직 처리
 * 2. 트랜잭션 관리 (@Transactional)
 * 3. Entity ↔ DTO 변환
 * 4. 예외 처리
 * 5. Repository 호출
 *
 * ==================================================================
 * 📝 구현 순서 (추천)
 * ==================================================================
 * 1. convertToDetailResponse() - 가장 기본적인 변환 메서드
 * 2. convertToAnswerResponse() - 답변 변환
 * 3. convertToListResponse() - 목록 변환
 * 4. createInquiry() - 생성 (가장 간단)
 * 5. getMyInquiries() - 목록 조회
 * 6. getInquiryDetail() - 상세 조회
 * 7. deleteInquiry() - 삭제
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;

    /**
     * ==================================================================
     * 1. 문의 생성 ⭐ 가장 먼저 구현하세요!
     * ==================================================================
     *
     * 📌 구현 단계:
     * STEP 1: Request DTO에서 데이터 꺼내기
     *         → request.getTitle(), request.getInquiryCategory(), request.getContent()
     *
     * STEP 2: Inquiry 엔티티 생성 (Builder 패턴 사용)
     *         → Inquiry.builder()
     *              .userId(userId)           // 파라미터로 받은 userId
     *              .title(request.getTitle())
     *              .inquiryCategory(request.getInquiryCategory())
     *              .content(request.getContent())
     *              .build();
     *         ⚠️ status는 Inquiry 엔티티의 기본값(PENDING)으로 자동 설정됨
     *
     * STEP 3: Repository에 저장
     *         → Inquiry savedInquiry = inquiryRepository.save(inquiry);
     *         ℹ️ save() 메서드는 저장된 엔티티를 반환합니다
     *
     * STEP 4: Entity → DTO 변환
     *         → return convertToDetailResponse(savedInquiry);
     *         ℹ️ 아래에 있는 convertToDetailResponse() 메서드 사용
     *
     * ==================================================================
     * 💡 예제 코드 (주석을 풀고 구현하세요)
     * ==================================================================
     */
    @Transactional
    public InquiryDetailResponse createInquiry(Long userId, CreateInquiryRequest request) {

        Inquiry inquiry = Inquiry.builder()
             .userId(userId)
             .title(request.getTitle())
             .inquiryCategory(request.getInquiryCategory())
             .content(request.getContent())
             .build();

        Inquiry savedInquiry = inquiryRepository.save(inquiry);

        return convertToDetailResponse(savedInquiry);
    }

    /**
     * ==================================================================
     * 2. 내 문의 목록 조회 (페이징)
     * ==================================================================
     *
     * 📌 구현 단계:
     * STEP 1: page 파라미터를 Pageable로 변환 (page는 1부터 시작)
     * STEP 2: Repository에서 userId로 문의 목록 조회 (페이징)
     * STEP 3: Entity Page → DTO Page로 변환
     *
     * @param userId 사용자 ID
     * @param page 페이지 번호 (1부터 시작)
     * @return 페이징된 문의 목록
     */
    public Page<InquiryListResponse> getMyInquiries(Long userId, int page) {
        if (page < 1) {
            throw new InvalidInquiryPageException(page);
        }

        // STEP 1: Pageable 생성 (page-1: 1-based → 0-based)
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page - 1,  // 0-based 인덱스로 변환
                10,        // 페이지당 10개
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
        );

        // STEP 2: Repository에서 userId로 문의 목록 조회 (페이징)
        Page<Inquiry> inquiries = inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<Inquiry> inquiryContent = inquiries.getContent();
        Set<Long> answeredInquiryIds = getAnsweredInquiryIds(inquiryContent);

        // STEP 3: Entity Page → DTO Page로 변환
        return inquiries.map(inquiry -> convertToListResponse(
                inquiry,
                answeredInquiryIds.contains(inquiry.getId())
        ));
    }

    /**
     * ==================================================================
     * 3. 문의 상세 조회
     * ==================================================================
     *
     * 📌 구현 단계:
     * STEP 1: Repository에서 inquiryId로 문의 조회
     *         → Inquiry inquiry = inquiryRepository.findById(inquiryId)
     *              .orElseThrow(() -> new InquiryNotFoundException(inquiryId));
     *
     *         ℹ️ Optional 설명:
     *         - findById()는 Optional<Inquiry>를 반환
     *         - orElseThrow()는 값이 없으면 예외를 던짐
     *
     * STEP 2: 본인의 문의인지 확인 (보안 검증)
     *         → if (!inquiry.getUserId().equals(userId)) {
     *              throw new UnauthorizedInquiryAccessException();
     *           }
     *
     *         ℹ️ 왜 필요한가?
     *         - 다른 사람의 문의를 볼 수 없게 하기 위함
     *         - equals() 사용 이유: Long 타입은 객체이므로 == 대신 equals() 사용
     *
     * STEP 3: Entity → DTO 변환
     *         → return convertToDetailResponse(inquiry);
     */
    public InquiryDetailResponse getInquiryDetail(Long userId, Long inquiryId) {
        // STEP 1: inquiryId로 문의 조회
         Inquiry inquiry = inquiryRepository.findById(inquiryId)
             .orElseThrow(() -> new InquiryNotFoundException(inquiryId));

        // STEP 2: 본인 문의인지 확인
         if (!inquiry.getUserId().equals(userId)) {
             throw new UnauthorizedInquiryAccessException();
         }

        // STEP 3: Entity -> DTO 변환
         return convertToDetailResponse(inquiry);
    }



    /**
     * ==================================================================
     * Inquiry 엔티티 → InquiryDetailResponse 변환 ⭐ 가장 먼저 구현!
     * ==================================================================
     *
     * 📌 구현 단계:
     * STEP 1: InquiryAnswer 조회 (Optional)
     *         → InquiryAnswerResponse answerResponse = inquiryAnswerRepository
     *              .findByInquiryId(inquiry.getId())  // Optional<InquiryAnswer> 반환
     *              .map(this::convertToAnswerResponse)  // Answer 있으면 DTO로 변환
     *              .orElse(null);  // 없으면 null
     *
     *         ℹ️ Optional.map() 설명:
     *         - 값이 있으면 변환 함수 적용
     *         - 값이 없으면 아무것도 안 함
     *         - orElse(null): 값이 없으면 null 반환
     *
     * STEP 2: InquiryDetailResponse Builder로 생성
     *         → return InquiryDetailResponse.builder()
     *              .id(inquiry.getId())
     *              .userId(inquiry.getUserId())
     *              .title(inquiry.getTitle())
     *              .inquiryCategory(inquiry.getInquiryCategory())
     *              .content(inquiry.getContent())
     *              .status(inquiry.getStatus())
     *              .createdAt(inquiry.getCreatedAt())
     *              .updatedAt(inquiry.getUpdatedAt())
     *              .answer(answerResponse)  // STEP 1에서 구한 답변 (null 가능)
     *              .build();
     *
     */
    private InquiryDetailResponse convertToDetailResponse(Inquiry inquiry) {
        // STEP 1: InquiryAnswer 조회 (있으면 DTO로 변환, 없으면 null)
         InquiryAnswerResponse answerResponse = inquiryAnswerRepository
             .findByInquiry_Id(inquiry.getId())
             .map(this::convertToAnswerResponse)
             .orElse(null);

        // STEP 2: InquiryDetailResponse 생성
         return InquiryDetailResponse.builder()
             .id(inquiry.getId())
             .userId(inquiry.getUserId())
             .title(inquiry.getTitle())
             .inquiryCategory(inquiry.getInquiryCategory())
             .content(inquiry.getContent())
             .status(inquiry.getStatus())
             .createdAt(inquiry.getCreatedAt())
             .updatedAt(inquiry.getUpdatedAt())
             .answer(answerResponse)
             .build();
    }

    /**
     * ==================================================================
     * Inquiry 엔티티 → InquiryListResponse 변환
     * ==================================================================
     *
     * 📌 구현 단계:
     * STEP 1: 답변 존재 여부 확인
     *         → boolean hasAnswer = inquiryAnswerRepository.existsByInquiryId(inquiry.getId());
     *
     * STEP 2: InquiryListResponse Builder로 생성
     *         → return InquiryListResponse.builder()
     *              .id(inquiry.getId())
     *              .title(inquiry.getTitle())
     *              .inquiryCategory(inquiry.getInquiryCategory())
     *              .status(inquiry.getStatus())
     *              .createdAt(inquiry.getCreatedAt())
     *              .hasAnswer(hasAnswer)  // STEP 1에서 구한 값
     *              .build();
     */
    private InquiryListResponse convertToListResponse(Inquiry inquiry, boolean hasAnswer) {
        return InquiryListResponse.builder()
                .id(inquiry.getId())
                .title(inquiry.getTitle())
                .inquiryCategory(inquiry.getInquiryCategory())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .hasAnswer(hasAnswer)
                .build();
    }

    /**
     * ==================================================================
     * InquiryAnswer 엔티티 → InquiryAnswerResponse 변환
     * ==================================================================
     *
     * 📌 구현 단계:
     * STEP 1: InquiryAnswerResponse Builder로 생성
     *         → return InquiryAnswerResponse.builder()
     *              .id(answer.getId())
     *              .adminUserId(answer.getAdminUserId())
     *              .answerContent(answer.getAnswerContent())
     *              .createdAt(answer.getCreatedAt())
     *              .build();
     */
    private InquiryAnswerResponse convertToAnswerResponse(InquiryAnswer answer) {
        return InquiryAnswerResponse.builder()
                .id(answer.getId())
                .adminUserId(answer.getAdminUserId())
                .answerContent(answer.getAnswerContent())
                .answeredAt(answer.getAnsweredAt())
                .build();
    }

    private Set<Long> getAnsweredInquiryIds(List<Inquiry> inquiries) {
        if (inquiries.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> inquiryIds = inquiries.stream()
                .map(Inquiry::getId)
                .collect(Collectors.toList());

        List<Long> answeredIds = inquiryAnswerRepository.findAnsweredInquiryIds(inquiryIds);
        return new HashSet<>(answeredIds);
    }
}
