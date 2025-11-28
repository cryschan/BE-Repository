package io.github.cryschan.berepository.domain.blogtemplate.scheduler;

import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.service.BlogTemplateService;
import io.github.cryschan.berepository.domain.fashion.dto.response.SsadaguProductDto;
import io.github.cryschan.berepository.domain.fashion.service.FashionCrawlerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BlogTemplateScheduler 테스트")
class BlogTemplateSchedulerTest {

    @Mock
    private BlogTemplateService blogTemplateService;

    @Mock
    private FashionCrawlerService fashionCrawlerService;

    @InjectMocks
    private BlogTemplateScheduler blogTemplateScheduler;

    @Test
    @DisplayName("예약된 템플릿이 없으면 아무 작업도 하지 않음")
    void collectTemplatesForCurrentSlot_NoTemplates() {
        // given
        given(blogTemplateService.getTemplatesForTime(any(LocalTime.class)))
                .willReturn(List.of());

        // when
        blogTemplateScheduler.collectTemplatesForCurrentSlot();

        // then
        verify(blogTemplateService).getTemplatesForTime(any(LocalTime.class));
        verifyNoInteractions(fashionCrawlerService);
    }

    @Test
    @DisplayName("예약된 템플릿이 있으면 크롤링 및 처리 수행")
    void collectTemplatesForCurrentSlot_Success() {
        // given
        BlogTemplate template1 = createMockTemplate(1L, 10L, List.of("패딩", "구두"));
        BlogTemplate template2 = createMockTemplate(2L, 20L, List.of("코트"));

        List<SsadaguProductDto> products1 = List.of(
                createMockProduct("패딩"),
                createMockProduct("구두")
        );
        List<SsadaguProductDto> products2 = List.of(
                createMockProduct("코트")
        );

        given(blogTemplateService.getTemplatesForTime(any(LocalTime.class)))
                .willReturn(List.of(template1, template2));
        given(fashionCrawlerService.crawlProductsByCategories(template1.getCategories()))
                .willReturn(products1);
        given(fashionCrawlerService.crawlProductsByCategories(template2.getCategories()))
                .willReturn(products2);

        // when
        blogTemplateScheduler.collectTemplatesForCurrentSlot();

        // then
        verify(blogTemplateService).getTemplatesForTime(any(LocalTime.class));
        verify(fashionCrawlerService).crawlProductsByCategories(template1.getCategories());
        verify(fashionCrawlerService).crawlProductsByCategories(template2.getCategories());
    }

    @Test
    @DisplayName("템플릿에 카테고리가 없으면 크롤링을 건너뜀")
    void collectTemplatesForCurrentSlot_EmptyCategories() {
        // given
        BlogTemplate templateWithoutCategories = createMockTemplate(1L, 10L, List.of());

        given(blogTemplateService.getTemplatesForTime(any(LocalTime.class)))
                .willReturn(List.of(templateWithoutCategories));

        // when
        blogTemplateScheduler.collectTemplatesForCurrentSlot();

        // then
        verify(blogTemplateService).getTemplatesForTime(any(LocalTime.class));
        verifyNoInteractions(fashionCrawlerService);
    }

    @Test
    @DisplayName("크롤링된 상품이 없으면 블로그 생성을 건너뜀")
    void collectTemplatesForCurrentSlot_NoProductsCrawled() {
        // given
        BlogTemplate template = createMockTemplate(1L, 10L, List.of("존재하지않는카테고리"));

        given(blogTemplateService.getTemplatesForTime(any(LocalTime.class)))
                .willReturn(List.of(template));
        given(fashionCrawlerService.crawlProductsByCategories(template.getCategories()))
                .willReturn(List.of());

        // when
        blogTemplateScheduler.collectTemplatesForCurrentSlot();

        // then
        verify(fashionCrawlerService).crawlProductsByCategories(template.getCategories());
        // TODO 생성 서비스 호출 검증은 AI 서비스 구현 후 추가
    }

    @Test
    @DisplayName("하나의 템플릿 처리 실패해도 다른 템플릿은 계속 처리")
    void collectTemplatesForCurrentSlot_PartialFailure() {
        // given
        BlogTemplate template1 = createMockTemplate(1L, 10L, List.of("패딩"));
        BlogTemplate template2 = createMockTemplate(2L, 20L, List.of("구두"));

        given(blogTemplateService.getTemplatesForTime(any(LocalTime.class)))
                .willReturn(List.of(template1, template2));
        given(fashionCrawlerService.crawlProductsByCategories(template1.getCategories()))
                .willThrow(new RuntimeException("크롤링 실패"));
        given(fashionCrawlerService.crawlProductsByCategories(template2.getCategories()))
                .willReturn(List.of(createMockProduct("구두")));

        // when
        blogTemplateScheduler.collectTemplatesForCurrentSlot();

        // then
        verify(fashionCrawlerService).crawlProductsByCategories(template1.getCategories());
        verify(fashionCrawlerService).crawlProductsByCategories(template2.getCategories());
    }

    @Test
    @DisplayName("템플릿 카테고리가 null이면 크롤링을 건너뜀")
    void collectTemplatesForCurrentSlot_NullCategories() {
        // given
        BlogTemplate template = BlogTemplate.builder()
                .id(1L)
                .title("테스트 템플릿")
                .categories(null)  // null 카테고리
                .platforms(new ArrayList<>(List.of("네이버")))
                .shopUrl("https://shop.com")
                .includeImages(true)
                .imageCount(3)
                .charLimit(1000)
                .dailyPostTime(LocalTime.of(9, 0))
                .userId(10L)
                .build();

        given(blogTemplateService.getTemplatesForTime(any(LocalTime.class)))
                .willReturn(List.of(template));

        // when
        blogTemplateScheduler.collectTemplatesForCurrentSlot();

        // then
        verify(blogTemplateService).getTemplatesForTime(any(LocalTime.class));
        verifyNoInteractions(fashionCrawlerService);
    }

    private BlogTemplate createMockTemplate(Long id, Long userId, List<String> categories) {
        return BlogTemplate.builder()
                .id(id)
                .title("테스트 템플릿 " + id)
                .categories(new ArrayList<>(categories))
                .platforms(new ArrayList<>(List.of("네이버", "티스토리")))
                .shopUrl("https://shop.com")
                .includeImages(true)
                .imageCount(3)
                .charLimit(1000)
                .dailyPostTime(LocalTime.of(9, 0))
                .userId(userId)
                .build();
    }

    private SsadaguProductDto createMockProduct(String category) {
        return SsadaguProductDto.builder()
                .productName("테스트 " + category)
                .productUrl("https://ssadagu.kr/test")
                .price(29900)
                .rating(4.5)
                .reviewCount(100)
                .imageUrl("https://example.com/image.jpg")
                .category(category)
                .productAttributes(Map.of("소재", "폴리에스터"))
                .build();
    }
}
