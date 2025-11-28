package io.github.cryschan.berepository.domain.notice.service;

import io.github.cryschan.berepository.domain.notice.dto.NoticeCreateRequest;
import io.github.cryschan.berepository.domain.notice.dto.NoticeDetailDto;
import io.github.cryschan.berepository.domain.notice.dto.NoticeListItemDto;
import io.github.cryschan.berepository.domain.notice.entity.Notice;
import io.github.cryschan.berepository.domain.notice.exception.NoticeAccessDeniedException;
import io.github.cryschan.berepository.domain.notice.exception.NoticeNotFoundException;
import io.github.cryschan.berepository.domain.notice.repository.NoticeRepository;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    private static final int MAX_PAGE_SIZE = 100;

    /**
     * 공지사항 목록 조회
     *
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지 크기 (1~100)
     * @return 공지사항 목록 (중요 공지 우선, 최신순 정렬)
     */
    public Page<NoticeListItemDto> getNoticeList(int page, int size) {
        // 페이지 번호 검증
        if (page < 1) {
            throw new IllegalArgumentException("페이지 번호는 1 이상이어야 합니다.");
        }

        // 페이지 크기 제한 (Controller에서도 검증하지만, Service에서도 안전장치)
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        if (size < 1) {
            size = 10; // 기본값
        }

        // 1-based → 0-based 변환
        int pageIndex = page - 1;

        Pageable pageable = PageRequest.of(
                pageIndex,
                size,
                Sort.by(
                        Sort.Order.desc("isImportant"), // 중요 공지 먼저
                        Sort.Order.desc("createdAt")    // 다음 최신순
                )
        );

        Page<Notice> noticePage = noticeRepository.findAll(pageable);

        // 존재하지 않는 페이지 요청 체크
        if (page > 1 && noticePage.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("페이지 %d는 존재하지 않습니다. 전체 페이지 수: %d", page, noticePage.getTotalPages())
            );
        }

        return noticePage.map(NoticeListItemDto::from);
    }

    /**
     * 공지사항 단건 조회
     *
     * @param id     공지사항 ID
     * @param userId 인증된 사용자 ID (선택적, null 가능)
     * @return 공지사항 상세 정보
     * @throws NoticeNotFoundException 공지사항을 찾을 수 없는 경우
     */
    public NoticeDetailDto getNotice(Long id, Long userId) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeNotFoundException(id));

        // userId가 null이 아닐 때만 권한 확인 (DB 조회 최적화)
        boolean canEdit = userId != null && userRepository.findRoleById(userId)
                .map(role -> role == UserRole.ADMIN)
                .orElse(false);

        return NoticeDetailDto.from(notice, canEdit);
    }

    /**
     * 공지사항 생성
     *
     * @param request 공지사항 생성 요청
     * @param userId  인증된 사용자 ID (필수)
     * @return 생성된 공지사항 ID
     * @throws NoticeAccessDeniedException 관리자 권한이 없는 경우
     */
    @Transactional
    public Long createNotice(NoticeCreateRequest request, Long userId) {
        // 인증 체크
        if (userId == null) {
            throw new NoticeAccessDeniedException("인증이 필요합니다.");
        }

        // 관리자 권한 체크
        boolean isAdmin = userRepository.findRoleById(userId)
                .map(role -> role == UserRole.ADMIN)
                .orElse(false);

        if (!isAdmin) {
            throw new NoticeAccessDeniedException("공지사항 생성은 관리자만 가능합니다.");
        }

        Notice notice = Notice.builder()
                .title(request.title())
                .content(request.content())
                .isImportant(request.isImportant())
                .build();

        Notice savedNotice = noticeRepository.save(notice);

        return savedNotice.getId();
    }

}

