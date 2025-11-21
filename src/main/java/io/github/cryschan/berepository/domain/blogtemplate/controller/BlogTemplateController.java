package io.github.cryschan.berepository.domain.blogtemplate.controller;

import io.github.cryschan.berepository.domain.blogtemplate.dto.request.BlogTemplateCreateRequest;
import io.github.cryschan.berepository.domain.blogtemplate.dto.request.BlogTemplateUpdateRequest;
import io.github.cryschan.berepository.domain.blogtemplate.dto.response.BlogTemplateResponse;
import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.service.BlogTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog-templates")
public class BlogTemplateController {

    private final BlogTemplateService blogTemplateService;

    @PostMapping
    public ResponseEntity<BlogTemplateResponse> createTemplate(
            @Valid @RequestBody BlogTemplateCreateRequest request
    ) {
        BlogTemplate saved = blogTemplateService.createTemplate(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(BlogTemplateResponse.from(saved));
    }

    @PutMapping("/{templateId}")
    public BlogTemplateResponse updateTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody BlogTemplateUpdateRequest request
    ) {
        BlogTemplate updated = blogTemplateService.updateTemplate(
                templateId,
                request.title(),
                request.categoriesCopy(),
                request.platformsCopy(),
                request.shopUrl(),
                request.includeImages(),
                request.imageCount(),
                request.charLimit(),
                request.dailyPostTime()
        );
        return BlogTemplateResponse.from(updated);
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long templateId) {
        blogTemplateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{templateId}")
    public BlogTemplateResponse getTemplate(@PathVariable Long templateId) {
        return BlogTemplateResponse.from(blogTemplateService.getTemplate(templateId));
    }

    @GetMapping
    public List<BlogTemplateResponse> getAllTemplates() {
        return blogTemplateService.getAllTemplates().stream()
                .map(BlogTemplateResponse::from)
                .toList();
    }

    @GetMapping("/search")
    public List<BlogTemplateResponse> searchTemplates(
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> platforms
    ) {
        return blogTemplateService.searchTemplates(categories, platforms).stream()
                .map(BlogTemplateResponse::from)
                .toList();
    }

    @GetMapping("/schedule")
    public List<BlogTemplateResponse> getTemplatesForTime(@RequestParam("time") LocalTime postTime) {
        return blogTemplateService.getTemplatesForTime(postTime).stream()
                .map(BlogTemplateResponse::from)
                .toList();
    }
}
