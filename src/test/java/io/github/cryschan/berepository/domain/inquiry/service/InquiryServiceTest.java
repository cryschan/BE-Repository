package io.github.cryschan.berepository.domain.inquiry.service;

import io.github.cryschan.berepository.domain.inquiry.dto.Request.CreateInquiryRequest;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryDetailResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryListResponse;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.Inquiry;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryCategory;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryStatus;
import io.github.cryschan.berepository.domain.inquiry.exception.InvalidInquiryPageException;
import io.github.cryschan.berepository.domain.inquiry.exception.UnauthorizedInquiryAccessException;
import io.github.cryschan.berepository.domain.inquiry.repository.InquiryAnswerRepository;
import io.github.cryschan.berepository.domain.inquiry.repository.InquiryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("InquiryService 테스트")
class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private InquiryAnswerRepository inquiryAnswerRepository;

    @InjectMocks
    private InquiryService inquiryService;

    private Inquiry inquiry;
    private Inquiry savedInquiry;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        inquiry = Inquiry.builder()
                .id(1L)
                .userId(1L)
                .title("문의 제목")
                .inquiryCategory(InquiryCategory.FEATURE)
                .content("문의 내용")
                .build();
        ReflectionTestUtils.setField(inquiry, "createdAt", now);
        ReflectionTestUtils.setField(inquiry, "updatedAt", now);

        savedInquiry = Inquiry.builder()
                .id(2L)
                .userId(1L)
                .title("저장된 문의")
                .inquiryCategory(InquiryCategory.ACCOUNT)
                .content("저장된 문의 내용")
                .build();
        ReflectionTestUtils.setField(savedInquiry, "createdAt", now);
        ReflectionTestUtils.setField(savedInquiry, "updatedAt", now);
    }

    @Test
    @DisplayName("성공: 문의를 생성하고 상세 응답을 반환한다")
    void createInquiry_Success() {
        // given
        CreateInquiryRequest request = new CreateInquiryRequest("문의", InquiryCategory.FEATURE, "내용");
        given(inquiryRepository.save(any(Inquiry.class))).willReturn(savedInquiry);
        given(inquiryAnswerRepository.findByInquiry_Id(anyLong())).willReturn(Optional.empty());

        // when
        InquiryDetailResponse response = inquiryService.createInquiry(1L, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(savedInquiry.getTitle());
        assertThat(response.getUserId()).isEqualTo(1L);
        verify(inquiryRepository).save(any(Inquiry.class));
        verify(inquiryAnswerRepository).findByInquiry_Id(savedInquiry.getId());
    }

    @Test
    @DisplayName("실패: 페이지 번호가 1 미만이면 InvalidInquiryPageException")
    void getMyInquiries_InvalidPage() {
        assertThatThrownBy(() -> inquiryService.getMyInquiries(1L, 0, null))
                .isInstanceOf(InvalidInquiryPageException.class);
    }

    @Test
    @DisplayName("성공: 내 문의 목록을 페이지로 조회한다")
    void getMyInquiries_Success() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Inquiry> inquiryPage = new PageImpl<>(List.of(inquiry), pageRequest, 1);
        given(inquiryRepository.findByUserIdOrderByCreatedAtDesc(1L, pageRequest)).willReturn(inquiryPage);
        given(inquiryAnswerRepository.findAnsweredInquiryIds(anyList())).willReturn(List.of(inquiry.getId()));

        // when
        Page<InquiryListResponse> result = inquiryService.getMyInquiries(1L, 1, null);

        // then
        assertThat(result.getContent()).hasSize(1);
        InquiryListResponse listResponse = result.getContent().get(0);
        assertThat(listResponse.getId()).isEqualTo(inquiry.getId());
        assertThat(listResponse.isHasAnswer()).isTrue();
        verify(inquiryRepository).findByUserIdOrderByCreatedAtDesc(1L, pageRequest);
    }

    @Test
    @DisplayName("성공: 상태로 필터링하여 내 문의 목록을 조회한다")
    void getMyInquiries_FilterByStatus() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Inquiry> inquiryPage = new PageImpl<>(List.of(inquiry), pageRequest, 1);
        given(inquiryRepository.findByUserIdAndStatusOrderByCreatedAtDesc(1L, InquiryStatus.COMPLETED, pageRequest)).willReturn(inquiryPage);
        given(inquiryAnswerRepository.findAnsweredInquiryIds(anyList())).willReturn(List.of());

        // when
        Page<InquiryListResponse> result = inquiryService.getMyInquiries(1L, 1, InquiryStatus.COMPLETED);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(inquiryRepository).findByUserIdAndStatusOrderByCreatedAtDesc(1L, InquiryStatus.COMPLETED, pageRequest);
    }

    @Test
    @DisplayName("실패: 다른 사용자의 문의는 조회할 수 없다")
    void getInquiryDetail_Unauthorized() {
        // given
        Inquiry otherUserInquiry = Inquiry.builder()
                .id(3L)
                .userId(2L)
                .title("타인 문의")
                .inquiryCategory(InquiryCategory.ACCOUNT)
                .content("타인 문의 내용")
                .build();
        given(inquiryRepository.getByIdOrThrow(3L)).willReturn(otherUserInquiry);

        // when & then
        assertThatThrownBy(() -> inquiryService.getInquiryDetail(1L, 3L))
                .isInstanceOf(UnauthorizedInquiryAccessException.class);
    }

    @Test
    @DisplayName("성공: 본인의 문의 상세를 조회한다")
    void getInquiryDetail_Success() {
        // given
        given(inquiryRepository.getByIdOrThrow(1L)).willReturn(inquiry);
        given(inquiryAnswerRepository.findByInquiry_Id(1L)).willReturn(Optional.empty());

        // when
        InquiryDetailResponse response = inquiryService.getInquiryDetail(1L, 1L);

        // then
        assertThat(response.getId()).isEqualTo(inquiry.getId());
        assertThat(response.getContent()).isEqualTo("문의 내용");
        assertThat(response.getStatus()).isEqualTo(InquiryStatus.PENDING);
        verify(inquiryRepository).getByIdOrThrow(1L);
    }
}
