package io.github.cryschan.berepository.domain.blog.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BlogUpdateRequest 검증 테스트")
class BlogUpdateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("제목(title) 검증 테스트")
    class TitleValidationTest {

        @Test
        @DisplayName("성공: 유효한 제목으로 검증을 통과한다")
        void valid_Title() {
            // given
            BlogUpdateRequest request = createValidRequest();

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("실패: 제목이 null인 경우 검증 실패")
        void title_Null() {
            // given
            BlogUpdateRequest request = createRequestWithTitle(null);

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("제목은 필수입니다");
        }

        @Test
        @DisplayName("실패: 제목이 빈 문자열인 경우 검증 실패")
        void title_Empty() {
            // given
            BlogUpdateRequest request = createRequestWithTitle("");

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("제목은 필수입니다");
        }

        @Test
        @DisplayName("실패: 제목이 공백만 있는 경우 검증 실패")
        void title_BlankSpaces() {
            // given
            BlogUpdateRequest request = createRequestWithTitle("   ");

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("제목은 필수입니다");
        }

        @Test
        @DisplayName("실패: 제목이 100자를 초과하는 경우 검증 실패")
        void title_ExceedsMaxLength() {
            // given
            String longTitle = "a".repeat(101);
            BlogUpdateRequest request = createRequestWithTitle(longTitle);

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("제목은 100자 이하여야 합니다");
        }

        @Test
        @DisplayName("성공: 제목이 정확히 100자인 경우 검증 통과")
        void title_ExactlyMaxLength() {
            // given
            String maxLengthTitle = "a".repeat(100);
            BlogUpdateRequest request = createRequestWithTitle(maxLengthTitle);

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("성공: 제목이 1자인 경우 검증 통과")
        void title_MinimumLength() {
            // given
            BlogUpdateRequest request = createRequestWithTitle("a");

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("내용(content) 검증 테스트")
    class ContentValidationTest {

        @Test
        @DisplayName("성공: 유효한 내용으로 검증을 통과한다")
        void valid_Content() {
            // given
            BlogUpdateRequest request = createValidRequest();

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("실패: 내용이 null인 경우 검증 실패")
        void content_Null() {
            // given
            BlogUpdateRequest request = createRequestWithContent(null);

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("내용은 필수입니다");
        }

        @Test
        @DisplayName("실패: 내용이 빈 문자열인 경우 검증 실패")
        void content_Empty() {
            // given
            BlogUpdateRequest request = createRequestWithContent("");

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("내용은 필수입니다");
        }

        @Test
        @DisplayName("실패: 내용이 공백만 있는 경우 검증 실패")
        void content_BlankSpaces() {
            // given
            BlogUpdateRequest request = createRequestWithContent("   ");

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("내용은 필수입니다");
        }

        @Test
        @DisplayName("성공: 마크다운 형식의 내용이 검증을 통과한다")
        void content_WithMarkdown() {
            // given
            String markdownContent = """
                    # 여름 반팔 티셔츠 추천
                    
                    여름철 필수 아이템인 반팔 티셔츠를 소개합니다.
                    
                    ![티셔츠1](https://bucket.s3.amazonaws.com/uploads/tshirt1.jpg)
                    
                    시원한 소재로 만들어져 착용감이 좋습니다.
                    """;
            BlogUpdateRequest request = createRequestWithContent(markdownContent);

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("성공: 긴 내용도 검증을 통과한다")
        void content_LongText() {
            // given
            String longContent = "a".repeat(10000);
            BlogUpdateRequest request = createRequestWithContent(longContent);

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("카테고리(category) 검증 테스트")
    class CategoryValidationTest {

        @Test
        @DisplayName("성공: 유효한 카테고리로 검증을 통과한다")
        void valid_Category() {
            // given
            BlogUpdateRequest request = createValidRequest();

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("실패: 카테고리가 null인 경우 검증 실패")
        void category_Null() {
            // given
            BlogUpdateRequest request = createRequestWithCategory(null);

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("카테고리는 필수입니다");
        }

        @Test
        @DisplayName("실패: 카테고리가 빈 문자열인 경우 검증 실패")
        void category_Empty() {
            // given
            BlogUpdateRequest request = createRequestWithCategory("");

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("카테고리는 필수입니다");
        }

        @Test
        @DisplayName("실패: 카테고리가 공백만 있는 경우 검증 실패")
        void category_BlankSpaces() {
            // given
            BlogUpdateRequest request = createRequestWithCategory("   ");

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("카테고리는 필수입니다");
        }
    }

    @Nested
    @DisplayName("블로그 템플릿 ID(blogTemplateId) 검증 테스트")
    class BlogTemplateIdValidationTest {

        @Test
        @DisplayName("성공: blogTemplateId가 있는 경우 검증을 통과한다")
        void blogTemplateId_Present() {
            // given
            BlogUpdateRequest request = createValidRequest();

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("성공: blogTemplateId가 null인 경우에도 검증을 통과한다 (선택적 필드)")
        void blogTemplateId_Null() {
            // given
            BlogUpdateRequest request = createRequestWithBlogTemplateId(null);

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("복합 검증 테스트")
    class CombinedValidationTest {

        @Test
        @DisplayName("실패: 모든 필수 필드가 null인 경우 3개의 검증 실패")
        void all_Required_Fields_Null() {
            // given
            BlogUpdateRequest request = new BlogUpdateRequest();

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(3);  // title, content, category
        }

        @Test
        @DisplayName("실패: 제목이 너무 길고 다른 필드가 null인 경우 4개의 검증 실패")
        void multiple_Violations() {
            // given
            BlogUpdateRequest request = createInvalidRequest();

            // when
            Set<ConstraintViolation<BlogUpdateRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(3);  // title(size), content, category
        }
    }

    // Helper methods
    private BlogUpdateRequest createValidRequest() {
        return createRequest("여름 반팔 티셔츠 추천", "여름철 필수 아이템...", "상의", 1L);
    }

    private BlogUpdateRequest createRequestWithTitle(String title) {
        return createRequest(title, "여름철 필수 아이템...", "상의", 1L);
    }

    private BlogUpdateRequest createRequestWithContent(String content) {
        return createRequest("여름 반팔 티셔츠 추천", content, "상의", 1L);
    }

    private BlogUpdateRequest createRequestWithCategory(String category) {
        return createRequest("여름 반팔 티셔츠 추천", "여름철 필수 아이템...", category, 1L);
    }

    private BlogUpdateRequest createRequestWithBlogTemplateId(Long blogTemplateId) {
        return createRequest("여름 반팔 티셔츠 추천", "여름철 필수 아이템...", "상의", blogTemplateId);
    }

    private BlogUpdateRequest createInvalidRequest() {
        return createRequest("a".repeat(101), null, null, 1L);
    }

    private BlogUpdateRequest createRequest(String title, String content, String category, Long blogTemplateId) {
        BlogUpdateRequest request = new BlogUpdateRequest();
        try {
            java.lang.reflect.Field titleField = BlogUpdateRequest.class.getDeclaredField("title");
            titleField.setAccessible(true);
            titleField.set(request, title);

            java.lang.reflect.Field contentField = BlogUpdateRequest.class.getDeclaredField("content");
            contentField.setAccessible(true);
            contentField.set(request, content);

            java.lang.reflect.Field categoryField = BlogUpdateRequest.class.getDeclaredField("category");
            categoryField.setAccessible(true);
            categoryField.set(request, category);

            java.lang.reflect.Field blogTemplateIdField = BlogUpdateRequest.class.getDeclaredField("blogTemplateId");
            blogTemplateIdField.setAccessible(true);
            blogTemplateIdField.set(request, blogTemplateId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create request", e);
        }
        return request;
    }
}