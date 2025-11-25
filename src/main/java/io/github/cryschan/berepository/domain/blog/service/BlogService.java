package io.github.cryschan.berepository.domain.blog.service;

import io.github.cryschan.berepository.domain.blog.dto.response.BlogPageResponse;
import io.github.cryschan.berepository.domain.blog.dto.response.BlogResponse;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
}