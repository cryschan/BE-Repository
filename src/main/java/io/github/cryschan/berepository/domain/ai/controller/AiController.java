package io.github.cryschan.berepository.domain.ai.controller;

import io.github.cryschan.berepository.domain.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AiController {

    private final AiService aiService;

    @PostMapping("/api/ai/comment")
    public String aiComment(@RequestBody String comment) {
        return aiService.comment(comment);
    }

}
