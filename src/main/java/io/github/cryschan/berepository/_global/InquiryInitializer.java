package io.github.cryschan.berepository._global;

import io.github.cryschan.berepository.domain.inquiry.entity.Answer.InquiryAnswer;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.Inquiry;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryCategory;
import io.github.cryschan.berepository.domain.inquiry.repository.InquiryAnswerRepository;
import io.github.cryschan.berepository.domain.inquiry.repository.InquiryRepository;
import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 애플리케이션 시작 시 초기 1:1 문의 데이터를 생성하는 클래스
 *
 * 생성 데이터:
 * - 일반 사용자의 문의 (PENDING, COMPLETED 상태 혼합)
 * - 관리자 답변 (COMPLETED 상태 문의에만)
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Order(5) // UserInitializer 이후에 실행되도록 설정
public class InquiryInitializer implements CommandLineRunner {

    // ==================== 의존성 주입 ====================
    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final UserRepository userRepository;

    /**
     * 애플리케이션 시작 시 실행되는 메서드
     *
     * 처리 흐름:
     * 1. 초기화 시작 로그 출력
     * 2. 문의 데이터 중복 확인
     * 3. 초기 문의 및 답변 데이터 생성
     * 4. 완료 로그 출력
     */
    @Override
    @Transactional
    public void run(String... args) {
        log.info("=".repeat(80));
        log.info("Starting Initial Inquiry Data Initialization...");
        log.info("=".repeat(80));

        try {
            initializeInquiriesIfNotExists();

            log.info("=".repeat(80));
            log.info("Initial Inquiry Data Initialization Completed Successfully");
            log.info("=".repeat(80));
        } catch (Exception e) {
            log.error("=".repeat(80));
            log.error("Initial Inquiry Data Initialization Failed!", e);
            log.error("=".repeat(80));
            throw e;
        }
    }

    /**
     * 문의 데이터가 없을 때만 초기 데이터 생성
     *
     * 처리 흐름:
     * STEP 1: 기존 문의 데이터 존재 여부 확인
     * STEP 2: 데이터가 있으면 스킵
     * STEP 3: 일반 사용자 및 관리자 계정 조회
     * STEP 4: 초기 문의 데이터 생성 (다양한 카테고리, 상태 혼합)
     * STEP 5: 문의 저장
     * STEP 6: 답변이 필요한 문의에 대해 답변 생성
     * STEP 7: 답변 저장 및 문의 상태 COMPLETED로 변경
     * STEP 8: 생성 완료 로그 출력
     */
    private void initializeInquiriesIfNotExists() {
        log.debug("Checking if Inquiry data exists...");

        // STEP 1-2: 기존 문의 데이터가 있으면 스킵
        if (inquiryRepository.count() > 0) {
            log.info("[SKIP] Inquiry data already exists. Count: {}", inquiryRepository.count());
            return;
        }

        log.debug("Inquiry data does not exist. Creating initial inquiry data...");

        // STEP 3: 일반 사용자 및 관리자 계정 조회
        // ⚠️ UserInitializer에서 생성한 사용자 계정 사용
        Optional<User> generalUser = userRepository.findByEmail("user@repository.com");
        Optional<User> adminUser = userRepository.findByEmail("admin@repository.com");

        if (generalUser.isEmpty()) {
            log.warn("[SKIP] General user not found. Please run UserInitializer first.");
            return;
        }

        if (adminUser.isEmpty()) {
            log.warn("[SKIP] Admin user not found. Please run UserInitializer first.");
            return;
        }

        Long userId = generalUser.get().getUserId();
        Long adminId = adminUser.get().getUserId();

        // STEP 4: 초기 문의 데이터 생성
        List<Inquiry> initialInquiries = createInitialInquiries(userId);

        // STEP 5: 문의 저장
        List<Inquiry> savedInquiries = inquiryRepository.saveAll(initialInquiries);
        log.info("[CREATED] {} Inquiry entries successfully created", savedInquiries.size());

        // STEP 6-7: 일부 문의에 대해 답변 생성 (COMPLETED 상태로 변경)
        List<InquiryAnswer> answers = createInitialAnswers(savedInquiries, adminId);
        inquiryAnswerRepository.saveAll(answers);
        log.info("[CREATED] {} InquiryAnswer entries successfully created", answers.size());

        // STEP 8: 생성 완료 로그
        log.debug("Inquiry data initialization completed:");
        savedInquiries.forEach(inquiry ->
                log.debug("  - Inquiry #{}: {} (Category: {}, Status: {})",
                        inquiry.getId(),
                        inquiry.getTitle(),
                        inquiry.getInquiryCategory(),
                        inquiry.getStatus())
        );
    }

    /**
     * 초기 문의 데이터 생성
     *
     * 생성할 문의:
     * 1. 서비스 이용 관련 (미답변)
     * 2. 계정/결제 관련 (답변 완료)
     * 3. 기능 문의 (답변 완료)
     * 4. 오류 신고 (미답변)
     * 5. 제안 (답변 완료)
     * 6. 기타 (미답변)
     *
     * @param userId 문의 작성자 ID
     * @return 생성된 문의 리스트
     */
    private List<Inquiry> createInitialInquiries(Long userId) {
        List<Inquiry> inquiries = new ArrayList<>();


        //1. 서비스 이용 관련 문의 (PENDING)
         inquiries.add(Inquiry.builder()
                 .userId(userId)
                 .title("블로그 생성 시 오류가 발생합니다")
                 .inquiryCategory(InquiryCategory.FEATURE)
                 .content("AI 블로그 생성 버튼을 클릭하면 '서버 오류' 메시지가 표시됩니다...")
                 .build());

         //2. 계정/결제 관련 문의 (답변 예정)
         inquiries.add(Inquiry.builder()
                 .userId(userId)
                 .title("프리미엄 플랜 결제 후 기능이 활성화되지 않습니다")
                 .inquiryCategory(InquiryCategory.PAYMENT)
                 .content("어제 프리미엄 플랜을 결제했는데 아직 프리미엄 기능이 활성화되지 않았습니다...")
                 .build());

         //3. 기능 문의 (답변 예정)
         inquiries.add(Inquiry.builder()
                 .userId(userId)
                 .title("블로그 템플릿 변경 방법 문의")
                 .inquiryCategory(InquiryCategory.FEATURE)
                 .content("작성한 블로그의 템플릿을 다른 스타일로 변경하고 싶은데 방법을 모르겠습니다...")
                 .build());

         //4. 오류 신고 (PENDING)
         inquiries.add(Inquiry.builder()
                 .userId(userId)
                 .title("대시보드 통계가 정확하지 않습니다")
                 .inquiryCategory(InquiryCategory.FEATURE)
                 .content("블로그 조회수가 실제와 다르게 표시되는 것 같습니다...")
                 .build());

         //5. 제안 (답변 예정)
         inquiries.add(Inquiry.builder()
                 .userId(userId)
                 .title("다크모드 지원 요청")
                 .inquiryCategory(InquiryCategory.FEATURE)
                 .content("야간에 작업할 때 눈이 부셔서 다크모드 기능을 추가해주시면 좋겠습니다...")
                 .build());

         //6. 기타 (PENDING)
         inquiries.add(Inquiry.builder()
                 .userId(userId)
                 .title("API 문서 요청")
                 .inquiryCategory(InquiryCategory.ETC)
                 .content("외부 시스템과 연동하려고 하는데 API 문서가 있나요?...")
                 .build());

        return inquiries;
    }

    /**
     * 초기 답변 데이터 생성
     *
     * 일부 문의(예: 짝수 번째)에 대해서만 답변 생성
     * 답변이 생성된 문의는 상태가 COMPLETED로 변경됨
     *
     * @param inquiries 저장된 문의 리스트
     * @param adminId 답변 작성자(관리자) ID
     * @return 생성된 답변 리스트
     */
    private List<InquiryAnswer> createInitialAnswers(List<Inquiry> inquiries, Long adminId) {
        List<InquiryAnswer> answers = new ArrayList<>();


         for (int i = 0; i < inquiries.size(); i++) {
             // 짝수 번째 문의에만 답변 생성 (예시)
             if (i % 2 == 1) {
                 Inquiry inquiry = inquiries.get(i);

                 InquiryAnswer answer = InquiryAnswer.builder()
                         .inquiry(inquiry)
                         .adminUserId(adminId)
                         .answerContent("안녕하세요. 문의해주셔서 감사합니다. 확인 후 답변드립니다...")
                         .answeredAt(LocalDateTime.now())
                         .build();

                 answers.add(answer);

                 // 문의 상태를 COMPLETED로 변경
                 inquiry.complete();
             }
         }

        return answers;
    }
}
