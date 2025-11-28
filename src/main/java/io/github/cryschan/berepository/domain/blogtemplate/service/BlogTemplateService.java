package io.github.cryschan.berepository.domain.blogtemplate.service;

import io.github.cryschan.berepository.domain.blogtemplate.dto.response.BlogTemplateResponse;
import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.exception.BlogTemplateException;
import io.github.cryschan.berepository.domain.blogtemplate.repository.BlogTemplateRepository;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
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
        if (!userRepository.existsById(userId)) {
            throw BlogTemplateException.accessDenied("유효하지 않은 사용자입니다");
        }
        blogTemplateRepository.findByUserId(userId).ifPresent(existing -> {
            throw BlogTemplateException.alreadyExists(userId);
        });
        return BlogTemplateResponse.from(createTemplate(template));
    }

    public BlogTemplateResponse getTemplateResponseByUserId(Long targetUserId, Long requesterId) {
        if (!targetUserId.equals(requesterId) && !isAdmin(requesterId)) {
            throw BlogTemplateException.accessDenied("본인 또는 관리자만 조회할 수 있습니다");
        }
        return blogTemplateRepository.findByUserId(targetUserId)
                .map(BlogTemplateResponse::from)
                .orElseThrow(() -> BlogTemplateException.notFoundByUserId(targetUserId));
    }

    @Transactional
    public BlogTemplateResponse updateTemplateByUserId(
            Long userId,
            Long requesterId,
            String title,
            List<String> categories,
            List<String> platforms,
            String shopUrl,
            boolean includeImages,
            int imageCount,
            int charLimit,
            LocalTime dailyPostTime
    ) {
        if (!userId.equals(requesterId) && !isAdmin(requesterId)) {
            throw BlogTemplateException.accessDenied("본인 또는 관리자만 수정할 수 있습니다");
        }
        BlogTemplate template = blogTemplateRepository.findByUserId(userId)
                .orElseThrow(() -> BlogTemplateException.notFoundByUserId(userId));
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
        return BlogTemplateResponse.from(template);
    }

    @Transactional
    public void deleteTemplateByUserId(Long userId, Long requesterId) {
        if (!userId.equals(requesterId) && !isAdmin(requesterId)) {
            throw BlogTemplateException.accessDenied("본인 또는 관리자만 삭제할 수 있습니다");
        }
        BlogTemplate template = blogTemplateRepository.findByUserId(userId)
                .orElseThrow(() -> BlogTemplateException.notFoundByUserId(userId));
        blogTemplateRepository.deleteById(template.getId());
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

    private boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRole() == UserRole.ADMIN)
                .orElse(false);
    }
}
