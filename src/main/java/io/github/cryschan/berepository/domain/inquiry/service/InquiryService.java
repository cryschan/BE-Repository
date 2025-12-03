package io.github.cryschan.berepository.domain.inquiry.service;

import io.github.cryschan.berepository.domain.inquiry.dto.Request.CreateInquiryRequest;
import io.github.cryschan.berepository.domain.inquiry.dto.Request.UpdateInquiryRequest;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryAnswerResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryDetailResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryListResponse;
import io.github.cryschan.berepository.domain.inquiry.entity.Answer.InquiryAnswer;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.Inquiry;
import io.github.cryschan.berepository.domain.inquiry.exception.InquiryAlreadyAnsweredException;
import io.github.cryschan.berepository.domain.inquiry.exception.InquiryNotFoundException;
import io.github.cryschan.berepository.domain.inquiry.exception.UnauthorizedInquiryAccessException;
import io.github.cryschan.berepository.domain.inquiry.repository.InquiryAnswerRepository;
import io.github.cryschan.berepository.domain.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
 * 7. updateInquiry() - 수정 (검증 로직 많음)
 * 8. deleteInquiry() - 삭제
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
     * 2. 내 문의 목록 조회
     * ==================================================================
     *
     * 📌 구현 단계:
     * STEP 1: Repository에서 userId로 문의 목록 조회
     *         → List<Inquiry> inquiries = inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId);
     *         ℹ️ 이미 InquiryRepository에 메서드가 정의되어 있음
     *
     * STEP 2: Stream API를 사용해서 Entity 리스트 → DTO 리스트로 변환
     *         → return inquiries.stream()
     *              .map(inquiry -> convertToListResponse(inquiry))  // 각 inquiry를 DTO로 변환
     *              .collect(Collectors.toList());                   // 리스트로 모으기
     *
     * ==================================================================
     * 💡 Stream API 설명
     * ==================================================================
     * - stream(): 리스트를 Stream으로 변환
     * - map(): 각 요소를 변환 (여기서는 Inquiry → InquiryListResponse)
     * - collect(Collectors.toList()): Stream을 다시 List로 변환
     *
     * 예시:
     * List<Integer> numbers = Arrays.asList(1, 2, 3);
     * List<Integer> doubled = numbers.stream()
     *     .map(n -> n * 2)  // 각 숫자를 2배로
     *     .collect(Collectors.toList());  // [2, 4, 6]
     */
    public List<InquiryListResponse> getMyInquiries(Long userId) {
        // STEP 1: Repository에서 userId로 문의 목록 조회
        List<Inquiry> inquiries = inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // 💡 람다식 간략화 가능:
         return inquiries.stream()
             .map(this::convertToListResponse)
             .collect(Collectors.toList());
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
     * 4. 문의 수정 ⚠️ 검증 로직이 많아서 조금 복잡합니다
     * ==================================================================
     *
     * 📌 구현 단계:
     * STEP 1: Repository에서 inquiryId로 문의 조회
     *         → Inquiry inquiry = inquiryRepository.findById(inquiryId)
     *              .orElseThrow(() -> new InquiryNotFoundException(inquiryId));
     *
     * STEP 2: 본인의 문의인지 확인
     *         → if (!inquiry.getUserId().equals(userId)) {
     *              throw new UnauthorizedInquiryAccessException();
     *           }
     *
     * STEP 3: 답변이 달렸는지 확인 (비즈니스 규칙)
     *         → if (inquiryAnswerRepository.existsByInquiryId(inquiryId)) {
     *              throw new InquiryAlreadyAnsweredException();
     *           }
     *
     *         ℹ️ 왜 필요한가?
     *         - 답변이 달린 문의는 수정할 수 없게 하는 정책
     *         - existsBy...() 메서드는 boolean 반환 (있으면 true, 없으면 false)
     *
     * STEP 4: Inquiry 엔티티의 비즈니스 메서드 호출
     *         → inquiry.updateContent(request.getTitle(), request.getContent());
     *
     *         ℹ️ JPA의 변경 감지 (Dirty Checking)
     *         - @Transactional 안에서 엔티티를 변경하면 자동으로 DB 업데이트됨
     *         - 따라서 save() 호출 불필요!
     *
     * STEP 5: Entity → DTO 변환
     *         → return convertToDetailResponse(inquiry);
     */
    @Transactional
    public InquiryDetailResponse updateInquiry(Long userId, Long inquiryId, UpdateInquiryRequest request) {
        // STEP 1: inquiryId로 문의 조회
         Inquiry inquiry = inquiryRepository.findById(inquiryId)
             .orElseThrow(() -> new InquiryNotFoundException(inquiryId));

        // STEP 2: 본인 문의인지 확인
         if (!inquiry.getUserId().equals(userId)) {
             throw new UnauthorizedInquiryAccessException();
         }

        // STEP 3: 답변 여부 확인 (답변 달린 문의는 수정 불가)
         if (inquiryAnswerRepository.existsByInquiryId(inquiryId)) {
             throw new InquiryAlreadyAnsweredException();
         }

        // STEP 4: Inquiry 엔티티의 updateContent() 메서드 호출
         inquiry.updateContent(request.getTitle(), request.getContent());
        // ℹ️ @Transactional 덕분에 자동으로 DB에 반영됨 (Dirty Checking)

        // STEP 5: Entity -> DTO 변환
         return convertToDetailResponse(inquiry);
    }

    /**
     * ==================================================================
     * 5. 문의 삭제
     * ==================================================================
     *
     * 📌 구현 단계:
     * STEP 1: Repository에서 inquiryId로 문의 조회
     * STEP 2: 본인의 문의인지 확인
     * STEP 3: (선택) 답변 여부 확인 - 정책에 따라 결정
     *         → 답변 달린 문의도 삭제 가능하게 할지?
     *         → 아니면 답변 달린 문의는 삭제 불가로 할지?
     * STEP 4: Repository에서 삭제
     *         → inquiryRepository.delete(inquiry);
     *         또는
     *         → inquiryRepository.deleteById(inquiryId);
     */
    @Transactional
    public void deleteInquiry(Long userId, Long inquiryId) {
        // STEP 1: inquiryId로 문의 조회
         Inquiry inquiry = inquiryRepository.findById(inquiryId)
             .orElseThrow(() -> new InquiryNotFoundException(inquiryId));

        // STEP 2: 본인 문의인지 확인
         if (!inquiry.getUserId().equals(userId)) {
             throw new UnauthorizedInquiryAccessException();
         }

        // STEP 3: (선택) 답변 여부 확인 - 정책에 따라
        // 답변 달린 문의는 삭제 불가로 하려면 아래 주석 해제
         if (inquiryAnswerRepository.existsByInquiryId(inquiryId)) {
             throw new InquiryAlreadyAnsweredException();
         }

        // STEP 4: 삭제
         inquiryRepository.deleteById(inquiryId);
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
             .findByInquiryId(inquiry.getId())
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
    private InquiryListResponse convertToListResponse(Inquiry inquiry) {
        // STEP 1: 답변 존재 여부 확인
        boolean hasAnswer = inquiryAnswerRepository.existsByInquiryId(inquiry.getId());

        // STEP 2: InquiryListResponse 생성
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
             .build();
    }
}
