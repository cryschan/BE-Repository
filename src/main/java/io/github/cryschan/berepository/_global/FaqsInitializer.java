package io.github.cryschan.berepository._global;

import io.github.cryschan.berepository.domain.faqs.entity.Faqs;
import io.github.cryschan.berepository.domain.faqs.entity.Statement;
import io.github.cryschan.berepository.domain.faqs.repository.FaqsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class FaqsInitializer implements CommandLineRunner {

    //의존성 주입
    private final FaqsRepository faqsRepository;

    //중복 데이터 방지
    @Override
    @Transactional
    public void run(String... args){
        //기존 데이터 확인
        log.info("=".repeat(80));
        log.info("Starting Initial FAQ Data Initialization...");
        log.info("=".repeat(80));

        try{
            initializeFaqsIfNotExists();

            log.info("=".repeat(80));
            log.info("Initial FAQ Data Initialization Complete!");
            log.info("=".repeat(80));
        }catch(Exception e){
            log.error("=".repeat(80));
            log.error("Initial FAQ Data Initialization Failed!", e);
            log.error("=".repeat(80));
            throw e;
        }


    }

    private void initializeFaqsIfNotExists() {
        log.debug("Checking if FAQ data exists...");

        // 기존 FAQ 데이터가 있으면 스킵
        if (faqsRepository.count() > 0) {
            log.info("[SKIP] FAQ data already exists");
            log.debug("FAQ initialization skipped - data already in database");
            return;
        }

        log.debug("FAQ data does not exist. Creating initial FAQ data...");

        //고정 FAQ 데이터 정의
        List<Faqs> initialFaqs = Arrays.asList(
                Faqs.builder()
                        .question("AI가 블로그 글을 생성하는 데 얼마나 걸리나요?")
                        .answer("일반적으로 키워드와 제품 URL을 입력한 후 약 30초에서 1분 정도 소요됩니다. 복잡한 카테고리나 긴 콘텐츠의 경우 조금 더 걸릴 수 있습니다.")
                        .sortOrder(1)
                        .statement(Statement.ACTIVE)
                        .build(),
                Faqs.builder()
                        .question("생성된 블로그 글을 수정할 수 있나요?")
                        .answer("네, 가능합니다. 블로그 글 편집 화면에서 AI가 생성한 콘텐츠를 자유롭게 수정하고 편집할 수 있습니다. 마크다운 형식을 지원하며 실시간 미리보기 기능도 제공됩니다.")
                        .sortOrder(2)
                        .statement(Statement.ACTIVE)
                        .build(),
                Faqs.builder()
                        .question("한 번에 여러 개의 카테고리를 선택할 수 있나요?")
                        .answer("회원가입 시 여러 카테고리를 선택하실 수 있습니다. 선택한 카테고리를 기반으로 AI가 다양한 키워드를 추출하고 관련 콘텐츠를 생성합니다.")
                        .sortOrder(3)
                        .statement(Statement.ACTIVE)
                        .build(),
                Faqs.builder()
                        .question("트래픽 통계는 어떻게 측정되나요?")
                        .answer("대시보드에서 각 블로그 글의 조회수, 클릭수, CTR(클릭률) 등을 실시간으로 확인할 수 있습니다. 데이터는 매일 업데이트됩니다.")
                        .sortOrder(4)
                        .statement(Statement.ACTIVE)
                        .build(),
                Faqs.builder()
                        .question("쇼핑몰 URL은 어떻게 활용되나요?")
                        .answer("AI가 입력하신 쇼핑몰 URL에서 제품 정보를 분석하여 관련 키워드를 추출하고, 해당 제품을 자연스럽게 홍보하는 블로그 글을 작성합니다.")
                        .sortOrder(5)
                        .statement(Statement.ACTIVE)
                        .build()
        );

        //데이터 저장
        List<Faqs> savedFaqs = faqsRepository.saveAll(initialFaqs);

        log.info("[CREATED] {} FAQ entries successfully created", savedFaqs.size());
        log.debug("FAQ data initialization completed:");
        savedFaqs.forEach(faq ->
            log.debug("  - FAQ #{}: {}", faq.getSortOrder(), faq.getQuestion())
        );
    }

}
