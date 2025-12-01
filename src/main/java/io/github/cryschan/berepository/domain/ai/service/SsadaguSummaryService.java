package io.github.cryschan.berepository.domain.ai.service;

import io.github.cryschan.berepository.domain.ai.exception.AiException;
import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SsadaguSummaryService {

    private final ChatClient chatClient;

    /**
     * AI 생성 결과 (제목 + 본문)
     */
    public record TitleAndSummary(String title, String summary) {}

    // System Prompt - AI의 역할과 성격 정의
    private static final String SYSTEM_PROMPT = """
            당신은 10년 경력의 전문 마케팅 카피라이터입니다.

            당신의 특징:
            - 고객의 구매 욕구를 자극하는 매력적인 문구 작성 전문가
            - 상품의 핵심 가치를 간결하고 임팩트 있게 전달
            - 감성적 어필과 실용적 정보를 균형 있게 조합
            - 자연스럽고 읽기 쉬운 한국어 사용

            작성 원칙:
            - 과장 없이 진정성 있는 홍보
            - 고객 관점에서 혜택 중심으로 작성
            - 행동을 유도하는 문구 포함
            """;

    // User Prompt with Few-shot Examples
    private static final String USER_TEMPLATE = """
            다음 상품 정보를 바탕으로 홍보용 제목과 본문을 작성하세요.

            [규칙]
            - 제목: 30자 이내, 클릭을 유도하는 매력적인 문구 (가격 정보 포함 가능)
            - 본문: 반드시 %d자 분량으로 마크다운 형식으로 작성
            - 본문은 ## 소제목, **강조**, - 리스트 등 마크다운 문법 활용
            - 자연스러운 한국어로 작성

            [출력 형식]
            [제목]
            여기에 제목 작성
            [본문]
            여기에 마크다운 본문 작성

            [좋은 예시]
            입력: 나이키 에어맥스, 129000원, 평점 4.5, 리뷰 1234개, 운동화
            출력:
            [제목]
            발걸음이 가벼워지는 에어맥스, 129,000원!
            [본문]
            ## 나이키 에어맥스 운동화

            발걸음마다 느껴지는 **편안함**! 나이키 에어맥스가 **129,000원**에 찾아왔습니다. 1,234명의 고객이 선택한 **4.5점**의 검증된 품질, 스타일과 기능을 동시에 잡은 운동화로 오늘부터 새로운 러닝을 시작하세요.

            ### 상품 정보
            - **가격**: 129,000원
            - **평점**: 4.5/5.0
            - **리뷰**: 1,234개

            ### 주요 특징
            - 뛰어난 쿠셔닝
            - 스타일리시한 디자인
            - 가벼운 착용감

            > 지금 바로 만나보세요!

            입력: 삼성 갤럭시 버즈3, 179000원, 평점 4.8, 리뷰 892개, 이어폰
            출력:
            [제목]
            프리미엄 사운드의 새로운 기준, 특가 179,000원
            [본문]
            ## 삼성 갤럭시 버즈3

            프리미엄 사운드를 일상으로! 삼성 갤럭시 버즈3가 **179,000원**의 합리적인 가격으로 만나보실 수 있습니다. 892개 리뷰에서 **4.8점**을 기록한 압도적 만족도, 선명한 음질과 완벽한 노이즈 캔슬링으로 나만의 음악 세계에 빠져보세요.

            ### 상품 정보
            - **가격**: 179,000원
            - **평점**: 4.8/5.0
            - **리뷰**: 892개

            ### 주요 특징
            - 선명한 음질
            - 완벽한 노이즈 캔슬링
            - 긴 배터리 수명

            > 지금 바로 만나보세요!

            [상품 정보]
            상품명: %s
            가격: %s
            평점: %s
            리뷰 수: %s개
            카테고리: %s
            %s
            """;

    public TitleAndSummary summaryWithTitle(SsadaguProductDto product, int charLimit) {

        if (product == null) {
            throw AiException.invalidProduct();
        }

        String attributesText = "";
        if (product.productAttributes() != null && !product.productAttributes().isEmpty()) {
            attributesText = "\n추가 정보 :\n" + product.productAttributes().entrySet().stream()
                    .map(e -> "- " + e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining("\n"));
        }

        String userPrompt = String.format(
                USER_TEMPLATE,
                charLimit,
                product.productName() != null ? product.productName() : "정보 없음",
                product.price() != null ? product.price() + "원" : "가격 정보 없음",
                product.rating() != null ? product.rating() + "/5.0" : "평점 없음",
                product.reviewCount() != null ? product.reviewCount() : "0",
                product.category() != null ? product.category() : "미분류",
                attributesText
        );

        String response = chatClient
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .content();

        return parseResponse(response, product.category());
    }

    /**
     * AI 응답 파싱 (제목/본문 분리)
     */
    private TitleAndSummary parseResponse(String response, String fallbackTitle) {
        String title = fallbackTitle != null ? fallbackTitle + " 추천 상품" : "추천 상품";
        String summary = response;

        try {
            if (response.contains("[제목]") && response.contains("[본문]")) {
                int titleStart = response.indexOf("[제목]") + "[제목]".length();
                int titleEnd = response.indexOf("[본문]");
                int summaryStart = titleEnd + "[본문]".length();

                title = response.substring(titleStart, titleEnd).trim();
                summary = response.substring(summaryStart).trim();
            }
        } catch (Exception e) {
            log.warn("AI 응답 파싱 실패, 기본값 사용: {}", e.getMessage());
        }

        return new TitleAndSummary(title, summary);
    }
}
