package io.github.cryschan.berepository.domain.blogtemplate.service;

import io.github.cryschan.berepository._global.exception.base.ErrorCode;
import io.github.cryschan.berepository.domain.blogtemplate.dto.response.BlogTemplateResponse;
import io.github.cryschan.berepository.domain.blogtemplate.entity.BlogTemplate;
import io.github.cryschan.berepository.domain.blogtemplate.exception.BlogTemplateException;
import io.github.cryschan.berepository.domain.blogtemplate.repository.BlogTemplateRepository;
import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("BlogTemplateService 테스트")
class BlogTemplateServiceTest {

    @Mock
    private BlogTemplateRepository blogTemplateRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BlogTemplateService blogTemplateService;

    private BlogTemplate defaultTemplate;

    @BeforeEach
    void setUp() {
        defaultTemplate = createTemplate(1L, 10L, true, 3);
    }

    @Test
    @DisplayName("템플릿 생성 시 이미지 옵션이 반영되고 저장된다")
    void createTemplateResponse_Success() {
        // given
        Long userId = 20L;
        BlogTemplate template = createTemplate(null, userId, false, 0);

        given(userRepository.existsById(userId)).willReturn(true);
        given(blogTemplateRepository.findByUserId(userId)).willReturn(Optional.empty());
        given(blogTemplateRepository.save(any(BlogTemplate.class))).willAnswer(AdditionalAnswers.returnsFirstArg());

        // when
        BlogTemplateResponse response = blogTemplateService.createTemplateResponse(userId, template);

        // then
        assertThat(response).isNotNull();
        assertThat(response.includeImages()).isFalse();
        assertThat(response.imageCount()).isZero(); // updateImageOptions가 적용되어야 함
        verify(blogTemplateRepository).save(template);
    }

    @Test
    @DisplayName("이미 템플릿이 있는 사용자는 생성 시 예외가 발생한다")
    void createTemplateResponse_AlreadyExists() {
        // given
        Long userId = 30L;
        given(userRepository.existsById(userId)).willReturn(true);
        given(blogTemplateRepository.findByUserId(userId)).willReturn(Optional.of(defaultTemplate));

        // when & then
        assertThatThrownBy(() -> blogTemplateService.createTemplateResponse(userId, defaultTemplate))
                .isInstanceOf(BlogTemplateException.class);
        verify(blogTemplateRepository, never()).save(any());
    }

    @Test
    @DisplayName("다른 사용자가 템플릿을 조회하면 접근 거부 예외가 발생한다")
    void getTemplateResponseByUserId_AccessDeniedForNonOwner() {
        // given
        Long targetUserId = 1L;
        Long requesterId = 2L;
        given(userRepository.findById(requesterId))
                .willReturn(Optional.of(User.builder().role(UserRole.USER).build()));

        // when & then
        assertThatThrownBy(() -> blogTemplateService.getTemplateResponseByUserId(targetUserId, requesterId))
                .isInstanceOf(BlogTemplateException.class);
        verifyNoInteractions(blogTemplateRepository);
    }

    @Test
    @DisplayName("본인은 자신의 템플릿을 조회할 수 있다")
    void getTemplateResponseByUserId_OwnerSuccess() {
        // given
        Long userId = 1L;
        BlogTemplate template = createTemplate(100L, userId, true, 2);

        given(blogTemplateRepository.findByUserId(userId)).willReturn(Optional.of(template));

        // when
        BlogTemplateResponse response = blogTemplateService.getTemplateResponseByUserId(userId, userId);

        // then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.platforms()).containsExactly("네이버");
        verify(blogTemplateRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("관리자는 다른 사용자의 템플릿을 사용자 ID로 조회할 수 있다")
    void getTemplateResponseByUserId_AdminSuccess() {
        // given
        Long adminId = 2L;
        Long targetUserId = 1L;
        BlogTemplate template = createTemplate(101L, targetUserId, true, 2);

        given(userRepository.findById(adminId)).willReturn(Optional.of(User.builder().role(UserRole.ADMIN).build()));
        given(blogTemplateRepository.findByUserId(targetUserId)).willReturn(Optional.of(template));

        // when
        BlogTemplateResponse response = blogTemplateService.getTemplateResponseByUserId(targetUserId, adminId);

        // then
        assertThat(response.id()).isEqualTo(101L);
        assertThat(response.platforms()).containsExactly("네이버");
    }

    @Test
    @DisplayName("관리자는 템플릿 ID로 단건 조회할 수 있다")
    void getTemplateResponseForAdmin_Success() {
        // given
        Long adminId = 2L;
        Long templateId = 10L;
        BlogTemplate template = createTemplate(templateId, 99L, true, 3);

        given(userRepository.findById(adminId)).willReturn(Optional.of(User.builder().role(UserRole.ADMIN).build()));
        given(blogTemplateRepository.findById(templateId)).willReturn(Optional.of(template));

        // when
        BlogTemplateResponse response = blogTemplateService.getTemplateResponseForAdmin(adminId, templateId);

        // then
        assertThat(response.id()).isEqualTo(templateId);
        assertThat(response.platforms()).containsExactly("네이버");
    }

    @Test
    @DisplayName("관리자가 아니면 템플릿 ID로 조회 시 예외가 발생한다")
    void getTemplateResponseForAdmin_AccessDenied() {
        // given
        Long requesterId = 3L;
        Long templateId = 11L;

        given(userRepository.findById(requesterId)).willReturn(Optional.of(User.builder().role(UserRole.USER).build()));

        // when & then
        assertThatThrownBy(() -> blogTemplateService.getTemplateResponseForAdmin(requesterId, templateId))
                .isInstanceOf(BlogTemplateException.class)
                .satisfies(ex -> assertThat(((BlogTemplateException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BLOG_TEMPLATE_ACCESS_DENIED));
        verify(blogTemplateRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("소유자는 템플릿을 수정할 수 있다")
    void updateTemplateByUserId_Success() {
        // given
        Long userId = 10L;
        BlogTemplate template = createTemplate(5L, userId, true, 3);
        given(blogTemplateRepository.findByUserId(userId)).willReturn(Optional.of(template));

        // when
        BlogTemplateResponse response = blogTemplateService.updateTemplateByUserId(
                userId,
                "수정된 제목",
                List.of("여행"),
                List.of("인스타그램"),
                "https://shop2.com",
                false,
                0,
                1500,
                LocalTime.NOON
        );

        // then
        assertThat(template.getTitle()).isEqualTo("수정된 제목");
        assertThat(template.getCategories()).containsExactly("여행");
        assertThat(template.getPlatforms()).containsExactly("인스타그램");
        assertThat(template.isIncludeImages()).isFalse();
        assertThat(template.getImageCount()).isZero();
        assertThat(response.charLimit()).isEqualTo(1500);
    }

    @Test
    @DisplayName("소유자는 자신의 템플릿을 삭제할 수 있다")
    void deleteTemplateByUserId_Success() {
        // given
        Long userId = 1L;
        BlogTemplate template = createTemplate(7L, userId, true, 3);

        given(blogTemplateRepository.findByUserId(userId)).willReturn(Optional.of(template));

        // when
        blogTemplateService.deleteTemplateByUserId(userId);

        // then
        verify(blogTemplateRepository).deleteById(7L);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 템플릿 삭제 시 예외가 발생한다")
    void deleteTemplateByUserId_NotFound() {
        // given
        Long userId = 999L;
        given(blogTemplateRepository.findByUserId(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> blogTemplateService.deleteTemplateByUserId(userId))
                .isInstanceOf(BlogTemplateException.class);
        verify(blogTemplateRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("카테고리 입력이 없으면 빈 리스트를 반환하고 조회하지 않는다")
    void getTemplatesByCategories_NoInput() {
        // when
        List<BlogTemplate> nullResult = blogTemplateService.getTemplatesByCategories(null);
        List<BlogTemplate> emptyResult = blogTemplateService.getTemplatesByCategories(List.of());

        // then
        assertThat(nullResult).isEmpty();
        assertThat(emptyResult).isEmpty();
        verifyNoInteractions(blogTemplateRepository);
    }

    @Test
    @DisplayName("카테고리와 플랫폼 검색 결과가 합쳐질 때 중복 템플릿은 한 번만 반환된다")
    void searchTemplateResponses_DeduplicatesResults() {
        // given
        BlogTemplate categoryTemplate = createTemplate(1L, 1L, true, 2);
        BlogTemplate platformTemplate = createTemplate(2L, 2L, true, 2);

        Collection<String> categories = Set.of("패션");
        Collection<String> platforms = Set.of("인스타그램");

        given(blogTemplateRepository.findByAnyCategory(categories))
                .willReturn(List.of(categoryTemplate));
        given(blogTemplateRepository.findByAnyPlatform(platforms))
                .willReturn(List.of(categoryTemplate, platformTemplate));

        // when
        List<BlogTemplateResponse> responses = blogTemplateService.searchTemplateResponses(
                categories,
                platforms
        );

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(1).id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 템플릿 생성 시 접근 거부 예외가 발생한다")
    void createTemplateResponse_InvalidUser() {
        // given
        Long userId = 99L;
        BlogTemplate template = createTemplate(null, userId, true, 3);
        given(userRepository.existsById(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> blogTemplateService.createTemplateResponse(userId, template))
                .isInstanceOf(BlogTemplateException.class)
                .satisfies(ex -> assertThat(((BlogTemplateException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BLOG_TEMPLATE_ACCESS_DENIED));
    }

    @Test
    @DisplayName("이미지 포함인데 imageCount가 0이면 잘못된 입력 예외가 발생한다")
    void createTemplateResponse_InvalidImageCount() {
        // given
        Long userId = 50L;
        BlogTemplate template = createTemplate(null, userId, true, 0);
        given(userRepository.existsById(userId)).willReturn(true);
        given(blogTemplateRepository.findByUserId(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> blogTemplateService.createTemplateResponse(userId, template))
                .isInstanceOf(BlogTemplateException.class)
                .satisfies(ex -> assertThat(((BlogTemplateException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_INPUT));
    }

    @Test
    @DisplayName("카테고리나 플랫폼이 null로 들어와도 빈 리스트로 처리해 업데이트된다")
    void updateTemplateByUserId_NullCollectionsHandled() {
        // given
        Long userId = 44L;
        BlogTemplate template = createTemplate(12L, userId, true, 2);
        given(blogTemplateRepository.findByUserId(userId)).willReturn(Optional.of(template));

        // when
        BlogTemplateResponse response = blogTemplateService.updateTemplateByUserId(
                userId,
                "제목",
                null,
                null,
                "https://shop2.com",
                false,
                0,
                500,
                LocalTime.of(8, 0)
        );

        // then
        assertThat(response.categories()).isEmpty();
        assertThat(response.platforms()).isEmpty();
        assertThat(response.includeImages()).isFalse();
        assertThat(response.imageCount()).isZero();
    }

    private BlogTemplate createTemplate(Long id, Long userId, boolean includeImages, int imageCount) {
        return BlogTemplate.builder()
                .id(id)
                .title("기본 템플릿")
                .categories(new ArrayList<>(List.of("패션")))
                .platforms(new ArrayList<>(List.of("네이버")))
                .shopUrl("https://shop.com")
                .includeImages(includeImages)
                .imageCount(imageCount)
                .charLimit(1000)
                .dailyPostTime(LocalTime.of(9, 0))
                .userId(userId)
                .build();
    }
}
