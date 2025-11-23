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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.List;
import java.security.Principal;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog-templates")
public class BlogTemplateController {

    private final BlogTemplateService blogTemplateService;

    @PostMapping
    public ResponseEntity<BlogTemplateResponse> createTemplate(
            @Valid @RequestBody BlogTemplateCreateRequest request,
            Principal principal
    ) {
        Long userId = extractUserId(principal);
        BlogTemplateResponse saved = blogTemplateService.createTemplateResponse(userId, request.toEntity(userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{templateId}")
    public BlogTemplateResponse updateTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody BlogTemplateUpdateRequest request,
            Principal principal
    ) {
        Long userId = extractUserId(principal);
        return blogTemplateService.updateTemplateResponse(
                userId,
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
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long templateId, Principal principal) {
        Long userId = extractUserId(principal);
        blogTemplateService.deleteTemplate(templateId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{templateId}")
    public BlogTemplateResponse getTemplate(@PathVariable Long templateId) {
        return blogTemplateService.getTemplateResponse(templateId);
    }

    @GetMapping
    public List<BlogTemplateResponse> getAllTemplates() {
        return blogTemplateService.getAllTemplateResponses();
    }

    @GetMapping("/me")
    public BlogTemplateResponse getMyTemplate(Principal principal) {
        Long userId = extractUserId(principal);
        return blogTemplateService.getTemplateResponseByUserId(userId, userId);
    }

    @GetMapping("/user/{userId}")
    public BlogTemplateResponse getTemplateByUser(@PathVariable Long userId, Principal principal) {
        Long requesterId = extractUserId(principal);
        return blogTemplateService.getTemplateResponseByUserId(userId, requesterId);
    }

    @GetMapping("/search")
    public List<BlogTemplateResponse> searchTemplates(
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> platforms
    ) {
        return blogTemplateService.searchTemplateResponses(categories, platforms);
    }

    @GetMapping("/schedule")
    public List<BlogTemplateResponse> getTemplatesForTime(@RequestParam("time") LocalTime postTime) {
        return blogTemplateService.getTemplateResponsesForTime(postTime);
    }

    private Long extractUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user principal");
        }
    }
}
