package io.github.cryschan.berepository.domain.faqs.controller;

import io.github.cryschan.berepository.domain.faqs.dto.response.FaqsResponse;
import io.github.cryschan.berepository.domain.faqs.service.FaqsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "FAQ", description = "FAQ 조회 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/faqs")
public class FaqsController {

    private final FaqsService faqsService;

    @Operation(summary = "FAQ 목록 조회", description = "정렬 순서 기준으로 FAQ를 조회합니다")
    @GetMapping
    public List<FaqsResponse> getFaqs() {
        return faqsService.getFaqs();
    }
}
