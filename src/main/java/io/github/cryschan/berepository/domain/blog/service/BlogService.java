package io.github.cryschan.berepository.domain.blog.service;

import io.github.cryschan.berepository._global.constants.DateConstants;
import io.github.cryschan.berepository.domain.ai.dto.response.SsadaguSummaryResponse;
import io.github.cryschan.berepository.domain.blog.dto.request.BlogUpdateRequest;
import io.github.cryschan.berepository.domain.blog.dto.response.BlogPageResponse;
import io.github.cryschan.berepository.domain.blog.dto.response.BlogResponse;
import io.github.cryschan.berepository.domain.blog.dto.response.BlogSaveResult;
import io.github.cryschan.berepository.domain.blog.entity.Blog;
import io.github.cryschan.berepository.domain.blog.exception.BlogException;
import io.github.cryschan.berepository.domain.blog.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;

    private static final int DEFAULT_PAGE_SIZE = 4;

    /**
     * 특정 유저의 블로그 목록 조회 (페이지네이션)
     */
    public BlogPageResponse getMyBlogs(Long userId, int page) {
        log.debug("Fetching blogs for userId: {}, page: {}", userId, page);

        // 페이지 번호 검증
        if (page < 1) {
            throw BlogException.invalidPage();
        }

        // 1-based → 0-based 변환
        int pageIndex = page - 1;
        Pageable pageable = PageRequest.of(pageIndex, DEFAULT_PAGE_SIZE);

        Page<Blog> blogPage = blogRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);

        // 존재하지 않는 페이지 요청 체크
        if (page > 1 && blogPage.isEmpty()) {
            throw BlogException.invalidPage(
                    String.format("페이지 %d는 존재하지 않습니다. 전체 페이지 수: %d", page, blogPage.getTotalPages())
            );
        }

        Page<BlogResponse> responsePage = blogPage.map(BlogResponse::from);

        log.debug("Found {} blogs for userId: {}", blogPage.getTotalElements(), userId);

        return BlogPageResponse.from(responsePage);
    }

    /**
     * 블로그 상세 조회
     */
    public BlogResponse getBlog(Long blogId) {
        log.debug("Fetching blog with id: {}", blogId);

        // ID 유효성 검증
        if (blogId == null || blogId <= 0) {
            throw BlogException.invalidId();
        }

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> BlogException.notFound(
                        String.format("블로그를 찾을 수 없습니다. id: %d", blogId)
                ));

        return BlogResponse.from(blog);
    }

    /**
     * 블로그 상세 조회 (권한 체크 포함)
     */
    public BlogResponse getBlogWithAuth(Long blogId, Long userId) {
        log.debug("Fetching blog with id: {} for userId: {}", blogId, userId);

        if (blogId == null || blogId <= 0) {
            throw BlogException.invalidId();
        }

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> BlogException.notFound(
                        String.format("블로그를 찾을 수 없습니다. id: %d", blogId)
                ));

        // 작성자만 조회 가능
        if (!blog.getUserId().equals(userId)) {
            throw BlogException.forbidden();
        }

        return BlogResponse.from(blog);
    }

    /**
     * 템플릿 정보와 AI 요약 결과로 블로그 일괄 생성 (스케줄러에서 호출)
     * <p>
     * 트랜잭션 동작: try-catch로 개별 저장 실패를 처리하여 부분 성공 허용.
     * 단, DB 연결 오류 등 예상치 못한 예외 시 전체 롤백될 수 있음.
     *
     * @param templateId    블로그 템플릿 ID
     * @param templateTitle 블로그 템플릿 제목
     * @param userId        사용자 ID
     * @param summaries     AI 요약 결과 목록
     * @return 저장 결과 (성공/실패 카운트)
     */
    @Transactional
    public BlogSaveResult createBlogsFromSummaries(Long templateId, String templateTitle,
                                                   Long userId, List<SsadaguSummaryResponse> summaries) {
        log.info("블로그 저장 시작 - templateId: {}, 요약 수: {}", templateId, summaries.size());

        int successCount = 0;
        int failCount = 0;

        for (SsadaguSummaryResponse summary : summaries) {
            // 9번: NULL 체크
            if (summary == null || summary.product() == null) {
                log.warn("유효하지 않은 summary, 건너뜀");
                failCount++;
                continue;
            }

            try {
                String title = generateBlogTitle(templateTitle, summary);
                String category = summary.product().category();

                if (category == null) {
                    log.warn("카테고리가 null, 건너뜀 - 상품명: {}", summary.product().productName());
                    failCount++;
                    continue;
                }

                Blog blog = Blog.create(templateId, title, summary.summary(), category, userId);
                Blog saved = blogRepository.save(blog);

                successCount++;
                // 8번: 개별 저장 성공은 debug 레벨
                log.debug("블로그 저장 성공 - id: {}, 제목: {}", saved.getId(), title);
            } catch (Exception e) {
                failCount++;
                log.error("블로그 저장 실패 - 상품명: {}, 에러: {}",
                        summary.product().productName(), e.getMessage(), e);
            }
        }

        log.info("블로그 저장 완료 - 성공: {}, 실패: {}", successCount, failCount);
        return BlogSaveResult.of(successCount, failCount);
    }

    /**
     * 블로그 제목 생성
     * 형식: [템플릿제목] 상품명 - 날짜
     */
    private String generateBlogTitle(String templateTitle, SsadaguSummaryResponse summary) {
        String date = LocalDate.now().format(DateConstants.BLOG_DATE_FORMATTER);
        String productName = summary.product().productName();

        if (productName != null && productName.length() > 50) {
            productName = productName.substring(0, 47) + "...";
        }

        return String.format("[%s] %s - %s", templateTitle, productName, date);
    }

    @Transactional
    public BlogResponse updateBlog(Long blogId, Long userId, BlogUpdateRequest request) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> BlogException.notFound(String.valueOf(blogId)));

        if (!blog.getUserId().equals(userId)) {
            throw BlogException.forbidden("해당 블로그를 수정할 권한이 없습니다");
        }

        blog.update(
                request.getTitle(),
                request.getContent(),
                request.getCategory()
        );

        return BlogResponse.from(blog);
    }
}