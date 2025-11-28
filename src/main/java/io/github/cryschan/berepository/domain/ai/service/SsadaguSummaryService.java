package io.github.cryschan.berepository.domain.ai.service;

import io.github.cryschan.berepository.domain.ai.exception.AiException;
import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SsadaguSummaryService {

    private final ChatClient chatClient;

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
            다음 상품 정보를 바탕으로 홍보 문구를 작성하세요.

            [규칙]
            - 반드시 %d자 분량으로 작성 (최대한 해당 글자수에 맞춰서 작성)
            - 줄바꿈 없이 한 문단으로 작성
            - 자연스러운 한국어로 작성

            [좋은 예시]
            입력: 나이키 에어맥스, 129000원, 평점 4.5, 리뷰 1234개, 운동화
            출력: 발걸음마다 느껴지는 편안함! 나이키 에어맥스가 129,000원에 찾아왔습니다. 1,234명의 고객이 선택한 4.5점의 검증된 품질, 스타일과 기능을 동시에 잡은 운동화로 오늘부터 새로운 러닝을 시작하세요.

            입력: 삼성 갤럭시 버즈3, 179000원, 평점 4.8, 리뷰 892개, 이어폰
            출력: 프리미엄 사운드를 일상으로! 삼성 갤럭시 버즈3가 179,000원의 합리적인 가격으로 만나보실 수 있습니다. 892개 리뷰에서 4.8점을 기록한 압도적 만족도, 선명한 음질과 완벽한 노이즈 캔슬링으로 나만의 음악 세계에 빠져보세요.

            [상품 정보]
            상품명: %s
            가격: %s
            평점: %s
            리뷰 수: %s개
            카테고리: %s
            %s
            """;

    public String summary(SsadaguProductDto product, int charLimit) {

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
        return chatClient
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .content();
    }


}
