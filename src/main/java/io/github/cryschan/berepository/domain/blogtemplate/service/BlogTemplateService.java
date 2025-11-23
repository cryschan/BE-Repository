package io.github.cryschan.berepository.domain.blogtemplate.service;

import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.repository.BlogTemplateRepository;
import io.github.cryschan.berepository.domain.blogtemplate.dto.response.BlogTemplateResponse;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BlogTemplateService {

    private final BlogTemplateRepository blogTemplateRepository;
    private final UserRepository userRepository;

    @Transactional
    public BlogTemplate createTemplate(BlogTemplate template) {
        template.updateImageOptions(template.isIncludeImages(), template.getImageCount());
        return blogTemplateRepository.save(template);
    }

    @Transactional
    public BlogTemplateResponse createTemplateResponse(Long userId, BlogTemplate template) {
        blogTemplateRepository.findByUserId(userId).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has a blog template");
        });
        if (template.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id is required for template creation");
        }
        return BlogTemplateResponse.from(createTemplate(template));
    }

    public BlogTemplate getTemplate(Long templateId) {
        return blogTemplateRepository.findById(templateId)
                .orElseThrow(() -> new NoSuchElementException("Blog template not found: " + templateId));
    }

    public BlogTemplateResponse getTemplateResponse(Long templateId) {
        return BlogTemplateResponse.from(getTemplate(templateId));
    }

    public BlogTemplateResponse getTemplateResponseByUserId(Long userId) {
        return blogTemplateRepository.findByUserId(userId)
                .map(BlogTemplateResponse::from)
                .orElseThrow(() -> new NoSuchElementException("Blog template not found for user: " + userId));
    }

    public BlogTemplateResponse getTemplateResponseByUserId(Long targetUserId, Long requesterId) {
        if (!targetUserId.equals(requesterId) && !isAdmin(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You may only access your own template");
        }
        return getTemplateResponseByUserId(targetUserId);
    }

    @Transactional
    public BlogTemplate updateTemplate(
            Long templateId,
            String title,
            List<String> categories,
            List<String> platforms,
            String shopUrl,
            boolean includeImages,
            int imageCount,
            int charLimit,
            LocalTime dailyPostTime
    ) {
        BlogTemplate template = getTemplate(templateId);
        template.updateTemplate(
                title,
                categories,
                platforms,
                shopUrl,
                includeImages,
                imageCount,
                charLimit,
                dailyPostTime
        );
        return template;
    }

    @Transactional
    public BlogTemplateResponse updateTemplateResponse(
            Long userId,
            Long templateId,
            String title,
            List<String> categories,
            List<String> platforms,
            String shopUrl,
            boolean includeImages,
            int imageCount,
            int charLimit,
            LocalTime dailyPostTime
    ) {
        ensureOwnerOrAdmin(templateId, userId);
        return BlogTemplateResponse.from(updateTemplate(
                templateId,
                title,
                categories,
                platforms,
                shopUrl,
                includeImages,
                imageCount,
                charLimit,
                dailyPostTime
        ));
    }

    @Transactional
    public void deleteTemplate(Long templateId, Long userId) {
        ensureOwnerOrAdmin(templateId, userId);
        blogTemplateRepository.deleteById(templateId);
    }

    public List<BlogTemplateResponse> getAllTemplateResponses() {
        return blogTemplateRepository.findAll().stream()
                .map(BlogTemplateResponse::from)
                .toList();
    }

    public List<BlogTemplate> getTemplatesByCategories(Collection<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }
        return blogTemplateRepository.findByAnyCategory(categories);
    }

    public List<BlogTemplate> getTemplatesByPlatforms(Collection<String> platforms) {
        if (platforms == null || platforms.isEmpty()) {
            return List.of();
        }
        return blogTemplateRepository.findByAnyPlatform(platforms);
    }

    public List<BlogTemplate> getTemplatesForTime(LocalTime postTime) {
        return blogTemplateRepository.findByDailyPostTime(postTime);
    }

    public List<BlogTemplateResponse> searchTemplateResponses(Collection<String> categories, Collection<String> platforms) {
        return searchTemplates(categories, platforms).stream()
                .map(BlogTemplateResponse::from)
                .toList();
    }

    public List<BlogTemplateResponse> getTemplateResponsesForTime(LocalTime postTime) {
        return getTemplatesForTime(postTime).stream()
                .map(BlogTemplateResponse::from)
                .toList();
    }

    private List<BlogTemplate> searchTemplates(Collection<String> categories, Collection<String> platforms) {
        boolean hasCategories = categories != null && !categories.isEmpty();
        boolean hasPlatforms = platforms != null && !platforms.isEmpty();

        if (!hasCategories && !hasPlatforms) {
            return List.of();
        }

        Set<BlogTemplate> collected = new LinkedHashSet<>();
        if (hasCategories) {
            collected.addAll(blogTemplateRepository.findByAnyCategory(categories));
        }
        if (hasPlatforms) {
            collected.addAll(blogTemplateRepository.findByAnyPlatform(platforms));
        }
        return List.copyOf(collected);
    }

    private void ensureOwnerOrAdmin(Long templateId, Long userId) {
        BlogTemplate template = getTemplate(templateId);
        if (!template.getUserId().equals(userId) && !isAdmin(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this template");
        }
    }

    private boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRole() == UserRole.ADMIN)
                .orElse(false);
    }
}
