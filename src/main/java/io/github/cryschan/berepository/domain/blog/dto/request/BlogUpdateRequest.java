package io.github.cryschan.berepository.domain.blog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "블로그 수정 요청")
public class BlogUpdateRequest {

    @Schema(description = "블로그 제목", example = "여름 반팔 티셔츠 추천")
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    private String title;

    @Schema(
            description = "블로그 내용 (이미지는 마크다운 형식으로 포함)",
            example = """
                    # 여름 반팔 티셔츠 추천
                    
                    여름철 필수 아이템인 반팔 티셔츠를 소개합니다.
                    
                    ![티셔츠1](https://bucket.s3.amazonaws.com/uploads/tshirt1.jpg)
                    
                    시원한 소재로 만들어져 착용감이 좋습니다.
                    
                    ![티셔츠2](https://bucket.s3.amazonaws.com/uploads/tshirt2.jpg)
                    """
    )
    @NotBlank(message = "내용은 필수입니다")
    private String content;

    @Schema(description = "카테고리", example = "상의", allowableValues = {"상의", "바지", "아우터", "신발", "가방", "패션소품"})
    @NotBlank(message = "카테고리는 필수입니다")
    private String category;

    @Schema(description = "블로그 템플릿 ID", example = "1")
    private Long blogTemplateId;
}