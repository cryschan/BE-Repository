package io.github.cryschan.berepository.domain.blog.service;

import io.github.cryschan.berepository.domain.blog.dto.response.BlogPageResponse;
import io.github.cryschan.berepository.domain.blog.dto.response.BlogResponse;
import io.github.cryschan.berepository.domain.blog.entity.Blog;
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

        // 1-based → 0-based 변환
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, DEFAULT_PAGE_SIZE);

        Page<Blog> blogPage = blogRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);

        Page<BlogResponse> responsePage = blogPage.map(BlogResponse::from);

        log.debug("Found {} blogs for userId: {}", blogPage.getTotalElements(), userId);

        return BlogPageResponse.from(responsePage);
    }

    /**
     * 블로그 상세 조회
     */
    public BlogResponse getBlog(Long blogId) {
        log.debug("Fetching blog with id: {}", blogId);

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new IllegalArgumentException("블로그를 찾을 수 없습니다. id: " + blogId));

        return BlogResponse.from(blog);
    }
}