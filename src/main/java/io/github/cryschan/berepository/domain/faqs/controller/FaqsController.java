package io.github.cryschan.berepository.domain.faqs.controller;

import io.github.cryschan.berepository.domain.faqs.dto.response.FaqsResponse;
import io.github.cryschan.berepository.domain.faqs.service.FaqsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "FAQ", description = "FAQ 조회 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/faqs")
public class FaqsController {

    private final FaqsService faqsService;

    @Operation(summary = "FAQ 목록 조회", description = "정렬 순서 기준으로 FAQ를 조회합니다")
    @ApiResponse(
            responseCode = "200",
            description = "FAQ 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = FaqsResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "빈 리스트",
                                    value = "[]"
                            ),
                            @ExampleObject(
                                    name = "FAQ 리스트",
                                    value = """
                                            [
                                              {
                                                "id": 1,
                                                "question": "서비스 이용 방법을 알려주세요",
                                                "answer": "회원가입 후 로그인하시면 모든 기능을 이용하실 수 있습니다.",
                                                "sortOrder": 1,
                                                "createdAt": "2024-01-15T10:00:00"
                                              },
                                              {
                                                "id": 2,
                                                "question": "비밀번호를 잊어버렸어요",
                                                "answer": "로그인 페이지의 비밀번호 찾기를 이용해주세요.",
                                                "sortOrder": 2,
                                                "createdAt": "2024-01-15T10:30:00"
                                              }
                                            ]
                                            """
                            )
                    }
            )
    )
    @GetMapping
    public List<FaqsResponse> getFaqs() {
        return faqsService.getFaqs();
    }
}
