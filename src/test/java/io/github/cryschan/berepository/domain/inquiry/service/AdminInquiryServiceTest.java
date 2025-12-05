package io.github.cryschan.berepository.domain.inquiry.service;

import io.github.cryschan.berepository.domain.inquiry.dto.Request.CreateAnswerRequest;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.AdminInquiryListResponse;
import io.github.cryschan.berepository.domain.inquiry.entity.Answer.InquiryAnswer;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.Inquiry;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryCategory;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryStatus;
import io.github.cryschan.berepository.domain.inquiry.exception.InquiryAlreadyAnsweredException;
import io.github.cryschan.berepository.domain.inquiry.exception.InquiryAnswerNotFoundException;
import io.github.cryschan.berepository.domain.inquiry.repository.InquiryAnswerRepository;
import io.github.cryschan.berepository.domain.inquiry.repository.InquiryRepository;
import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import io.github.cryschan.berepository.domain.user.exception.UserException;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
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
@DisplayName("AdminInquiryService 테스트")
class AdminInquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private InquiryAnswerRepository inquiryAnswerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InquiryService inquiryService;

    @InjectMocks
    private AdminInquiryService adminInquiryService;

    private User adminUser;
    private User inquiryOwner;
    private Inquiry inquiry;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .email("admin@test.com")
                .password("pw")
                .username("관리자")
                .role(UserRole.ADMIN)
                .build();
        ReflectionTestUtils.setField(adminUser, "userId", 100L);

        inquiryOwner = User.builder()
                .email("user@test.com")
                .password("pw")
                .username("사용자")
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(inquiryOwner, "userId", 1L);

        inquiry = Inquiry.builder()
                .id(10L)
                .userId(1L)
                .title("관리자 문의")
                .inquiryCategory(InquiryCategory.FEATURE)
                .content("문의 내용")
                .build();
        ReflectionTestUtils.setField(inquiry, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(inquiry, "updatedAt", LocalDateTime.now());
    }

    private void mockAdminValidation(Long adminId) {
        given(userRepository.findById(adminId)).willReturn(Optional.of(adminUser));
    }

    @Test
    @DisplayName("성공: 상태별 관리자 문의 목록을 조회한다")
    void getAllInquiries_FilterByStatus() {
        // given
        mockAdminValidation(100L);
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Inquiry> inquiryPage = new PageImpl<>(List.of(inquiry), pageable, 1);
        given(inquiryRepository.findByStatus(InquiryStatus.PENDING, pageable)).willReturn(inquiryPage);
        given(userRepository.findAllById(anyCollection())).willReturn(List.of(inquiryOwner));
        given(inquiryAnswerRepository.findAnsweredInquiryIds(anyList())).willReturn(List.of());

        // when
        Page<AdminInquiryListResponse> result = adminInquiryService.getAllInquiries(100L, InquiryStatus.PENDING, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        AdminInquiryListResponse response = result.getContent().get(0);
        assertThat(response.getUserEmail()).isEqualTo("user@test.com");
        verify(inquiryRepository).findByStatus(InquiryStatus.PENDING, pageable);
    }

    @Test
    @DisplayName("실패: 이미 답변된 문의에 다시 답변하려 하면 예외 발생")
    void answerInquiry_AlreadyAnswered() {
        // given
        mockAdminValidation(100L);
        given(inquiryRepository.getByIdOrThrow(10L)).willReturn(inquiry);
        given(inquiryAnswerRepository.existsByInquiry_Id(10L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> adminInquiryService.answerInquiry(100L, 10L, new CreateAnswerRequest("답변")))
                .isInstanceOf(InquiryAlreadyAnsweredException.class);
    }

    @Test
    @DisplayName("성공: 관리자 답변을 삭제하면 문의 상태가 PENDING으로 변경된다")
    void deleteAnswer_Success() {
        // given
        mockAdminValidation(100L);
        inquiry.complete();
        InquiryAnswer answer = InquiryAnswer.builder()
                .inquiry(inquiry)
                .adminUserId(100L)
                .answerContent("기존 답변")
                .answeredAt(LocalDateTime.now())
                .build();

        given(inquiryRepository.getByIdOrThrow(10L)).willReturn(inquiry);
        given(inquiryAnswerRepository.findByInquiry_Id(10L)).willReturn(Optional.of(answer));

        // when
        adminInquiryService.deleteAnswer(100L, 10L);

        // then
        assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.PENDING);
        verify(inquiryAnswerRepository).delete(answer);
    }

    @Test
    @DisplayName("실패: 관리자 검증 시 사용자가 없으면 예외 발생")
    void validateAdmin_UserNotFound() {
        // given
        given(userRepository.findById(200L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminInquiryService.getAllInquiries(200L, null, PageRequest.of(0, 10)))
                .isInstanceOf(UserException.class);
    }

    @Test
    @DisplayName("실패: 답변 삭제 시 답변을 찾지 못하면 예외 발생")
    void deleteAnswer_AnswerNotFound() {
        // given
        mockAdminValidation(100L);
        given(inquiryRepository.getByIdOrThrow(10L)).willReturn(inquiry);
        given(inquiryAnswerRepository.findByInquiry_Id(10L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminInquiryService.deleteAnswer(100L, 10L))
                .isInstanceOf(InquiryAnswerNotFoundException.class);
    }
}
