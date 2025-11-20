package io.github.cryschan.berepository.domain.blogtemplate.scheduler;

import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.service.BlogTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlogTemplateScheduler {

    private final BlogTemplateService blogTemplateService;

    @Scheduled(cron = "0 * * * * *")
    public void collectTemplatesForCurrentSlot() {
        LocalTime currentSlot = LocalTime.now().withSecond(0).withNano(0);
        List<BlogTemplate> templates = blogTemplateService.getTemplatesForTime(currentSlot);
        if (templates.isEmpty()) {
            return;
        }

        log.info("Found {} blog templates scheduled for {}", templates.size(), currentSlot);
        // TODO: Connect to AI generation + publishing flow using templates and product info.
    }
}

