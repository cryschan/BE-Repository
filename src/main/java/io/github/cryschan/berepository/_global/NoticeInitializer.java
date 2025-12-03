package io.github.cryschan.berepository._global;

import io.github.cryschan.berepository.domain.notice.entity.Notice;
import io.github.cryschan.berepository.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 애플리케이션 시작 시 초기 공지사항 데이터를 생성하는 클래스
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Order(4)
public class NoticeInitializer implements CommandLineRunner {

    private final NoticeRepository noticeRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=".repeat(80));
        log.info("Starting Initial Notice Data Initialization...");
        log.info("=".repeat(80));

        try {
            initializeNoticesIfNotExists();

            log.info("=".repeat(80));
            log.info("Initial Notice Data Initialization Completed Successfully");
            log.info("=".repeat(80));
        } catch (Exception e) {
            log.error("=".repeat(80));
            log.error("Initial Notice Data Initialization Failed!", e);
            log.error("=".repeat(80));
            throw e;
        }
    }

    private void initializeNoticesIfNotExists() {
        log.debug("Checking if Notice data exists...");

        // 기존 공지사항 데이터가 있으면 스킵
        if (noticeRepository.count() > 0) {
            log.info("[SKIP] Notice data already exists. Count: {}", noticeRepository.count());
            return;
        }

        log.debug("Notice data does not exist. Creating initial notice data...");

        LocalDateTime now = LocalDateTime.now();
        
        // 초기 공지사항 데이터 정의 (일부는 new로 표시되도록, 일부는 오래된 것으로)
        List<Notice> initialNotices = Arrays.asList(
                // 중요 공지사항 (new로 표시될 것들)
                Notice.builder()
                        .title("🚨 중요 공지: 시스템 점검 안내")
                        .content("""
                                안녕하세요.
                                
                                시스템 안정성 향상을 위한 정기 점검이 예정되어 있습니다.
                                
                                **점검 일시**
                                - 일시: 2024년 12월 15일 (일) 오전 2시 ~ 오전 6시
                                - 예상 소요 시간: 약 4시간
                                
                                **점검 내용**
                                - 서버 성능 최적화
                                - 데이터베이스 백업
                                - 보안 업데이트
                                
                                점검 시간 동안 서비스 이용이 제한될 수 있으니 양해 부탁드립니다.
                                
                                감사합니다.
                                """)
                        .isImportant(true)
                        .createdAt(now.minusDays(1))
                        .updatedAt(now.minusDays(1))
                        .build(),

                Notice.builder()
                        .title("✨ 새로운 기능 업데이트 안내")
                        .content("""
                                안녕하세요.
                                
                                더 나은 서비스 제공을 위해 새로운 기능이 추가되었습니다.
                                
                                **주요 업데이트 내용**
                                1. AI 블로그 생성 속도 개선 (약 30% 향상)
                                2. 대시보드 통계 기능 강화
                                3. 블로그 템플릿 추가 (5종)
                                4. 모바일 반응형 UI 개선
                                
                                **사용 방법**
                                - 대시보드에서 새로운 템플릿을 확인하실 수 있습니다.
                                - 블로그 생성 시 더 빠른 응답 속도를 경험하실 수 있습니다.
                                
                                자세한 내용은 FAQ를 참고해주세요.
                                
                                감사합니다.
                                """)
                        .isImportant(true)
                        .createdAt(now.minusDays(2))
                        .updatedAt(now.minusDays(2))
                        .build(),

                // 일반 공지사항 (new로 표시될 것)
                Notice.builder()
                        .title("📢 이용약관 개정 안내")
                        .content("""
                                안녕하세요.
                                
                                서비스 이용약관이 개정되어 안내드립니다.
                                
                                **개정 일자**: 2024년 12월 1일
                                
                                **주요 변경 사항**
                                - 개인정보 처리방침 보완
                                - 콘텐츠 저작권 관련 조항 명확화
                                - 서비스 이용 규칙 추가
                                
                                개정된 약관은 서비스 이용 시 적용되며, 계속 이용하시는 경우 개정 약관에 동의한 것으로 간주됩니다.
                                
                                자세한 내용은 [이용약관] 페이지에서 확인하실 수 있습니다.
                                
                                감사합니다.
                                """)
                        .isImportant(false)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),

                // 오래된 공지사항들 (new로 표시되지 않을 것들)
                Notice.builder()
                        .title("🎉 연말 이벤트 안내")
                        .content("""
                                안녕하세요.
                                
                                2024년을 마무리하며 특별 이벤트를 진행합니다!
                                
                                **이벤트 기간**
                                - 2024년 12월 20일 ~ 12월 31일
                                
                                **이벤트 내용**
                                - 이벤트 기간 중 블로그 10개 이상 작성 시 프리미엄 기능 1개월 무료 제공
                                - 신규 가입자 대상 웰컴 쿠폰 지급
                                
                                많은 참여 부탁드립니다!
                                
                                감사합니다.
                                """)
                        .isImportant(false)
                        .createdAt(now.minusDays(5))
                        .updatedAt(now.minusDays(5))
                        .build(),

                Notice.builder()
                        .title("📚 블로그 작성 가이드 업데이트")
                        .content("""
                                안녕하세요.
                                
                                더 나은 블로그 작성을 위한 가이드가 업데이트되었습니다.
                                
                                **업데이트 내용**
                                - SEO 최적화 팁 추가
                                - 키워드 선택 가이드 보완
                                - 이미지 활용 방법 안내
                                - 마크다운 작성 팁 추가
                                
                                **확인 방법**
                                - 대시보드 > 가이드 메뉴에서 확인하실 수 있습니다.
                                
                                더 나은 콘텐츠 작성을 위해 가이드를 참고해주세요.
                                
                                감사합니다.
                                """)
                        .isImportant(false)
                        .createdAt(now.minusDays(7))
                        .updatedAt(now.minusDays(7))
                        .build(),

                Notice.builder()
                        .title("🔒 보안 강화 안내")
                        .content("""
                                안녕하세요.
                                
                                서비스 보안을 강화하기 위한 조치를 진행했습니다.
                                
                                **보안 강화 내용**
                                - 2단계 인증(2FA) 기능 추가
                                - 비밀번호 정책 강화
                                - 로그인 이력 확인 기능 추가
                                
                                **권장 사항**
                                - 정기적인 비밀번호 변경
                                - 2단계 인증 활성화
                                - 의심스러운 활동 발견 시 즉시 고객센터로 문의
                                
                                보안 관련 문의사항이 있으시면 고객센터로 연락주세요.
                                
                                감사합니다.
                                """)
                        .isImportant(false)
                        .createdAt(now.minusDays(10))
                        .updatedAt(now.minusDays(10))
                        .build(),

                Notice.builder()
                        .title("💬 고객센터 운영 시간 안내")
                        .content("""
                                안녕하세요.
                                
                                고객센터 운영 시간을 안내드립니다.
                                
                                **운영 시간**
                                - 평일: 오전 9시 ~ 오후 6시
                                - 주말 및 공휴일: 휴무
                                
                                **문의 방법**
                                - 이메일: support@repository.com
                                - 문의 게시판: 24시간 접수 가능 (답변은 운영 시간 내 처리)
                                
                                문의사항이 있으시면 언제든지 연락주세요.
                                
                                감사합니다.
                                """)
                        .isImportant(false)
                        .createdAt(now.minusDays(15))
                        .updatedAt(now.minusDays(15))
                        .build(),

                Notice.builder()
                        .title("🎨 블로그 템플릿 신규 추가")
                        .content("""
                                안녕하세요.
                                
                                다양한 스타일의 블로그 템플릿이 추가되었습니다.
                                
                                **신규 템플릿**
                                1. 미니멀 스타일
                                2. 다크 모드
                                3. 카드 레이아웃
                                4. 매거진 스타일
                                5. 포트폴리오 스타일
                                
                                **사용 방법**
                                - 블로그 생성 시 템플릿 선택 메뉴에서 확인하실 수 있습니다.
                                - 템플릿은 언제든지 변경 가능합니다.
                                
                                다양한 템플릿으로 나만의 블로그를 꾸며보세요!
                                
                                감사합니다.
                                """)
                        .isImportant(false)
                        .createdAt(now.minusDays(20))
                        .updatedAt(now.minusDays(20))
                        .build()
        );

        // 데이터 저장
        List<Notice> savedNotices = noticeRepository.saveAll(initialNotices);

        log.info("[CREATED] {} Notice entries successfully created", savedNotices.size());
        log.debug("Notice data initialization completed:");
        savedNotices.forEach(notice ->
                log.debug("  - Notice #{}: {} (Important: {})", 
                        notice.getId(), 
                        notice.getTitle(), 
                        notice.isImportant())
        );
    }
}

