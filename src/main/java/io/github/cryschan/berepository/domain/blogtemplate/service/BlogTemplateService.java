package io.github.cryschan.berepository.domain.blogtemplate.service;

import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.repository.BlogTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public BlogTemplate createTemplate(BlogTemplate template) {
        template.updateImageOptions(template.isIncludeImages(), template.getImageCount());
        return blogTemplateRepository.save(template);
    }

    public BlogTemplate getTemplate(Long templateId) {
        return blogTemplateRepository.findById(templateId)
                .orElseThrow(() -> new NoSuchElementException("Blog template not found: " + templateId));
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
    public void deleteTemplate(Long templateId) {
        BlogTemplate template = getTemplate(templateId);
        blogTemplateRepository.delete(template);
    }

    public List<BlogTemplate> getAllTemplates() {
        return blogTemplateRepository.findAll();
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

    public List<BlogTemplate> searchTemplates(Collection<String> categories, Collection<String> platforms) {
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
}
