package io.github.cryschan.berepository.domain.inquiry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.cryschan.berepository._global.exception.handler.GlobalExceptionHandler;
import io.github.cryschan.berepository.domain.inquiry.dto.Request.CreateAnswerRequest;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.AdminInquiryListResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryDetailResponse;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryCategory;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryStatus;
import io.github.cryschan.berepository.domain.inquiry.service.AdminInquiryService;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminInquiryController 테스트")
class AdminInquiryControllerTest {

    @Mock
    private AdminInquiryService adminInquiryService;

    @InjectMocks
    private AdminInquiryController adminInquiryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(adminInquiryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("200 성공: 상태별 관리자 문의 목록 조회")
    void getAllInquiries_Success() throws Exception {
        AdminInquiryListResponse response = AdminInquiryListResponse.builder()
                .id(1L)
                .userId(1L)
                .userEmail("user@test.com")
                .userName("사용자")
                .title("문의 제목")
                .inquiryCategory(InquiryCategory.FEATURE)
                .status(InquiryStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .hasAnswer(false)
                .build();
        Page<AdminInquiryListResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        given(adminInquiryService.getAllInquiries(eq(100L), eq(InquiryStatus.PENDING), eq(1), eq(10)))
                .willReturn(page);

        mockMvc.perform(get("/api/admin/inquiries")
                        .with(authentication(new TestingAuthenticationToken(100L, null)))
                        .param("status", InquiryStatus.PENDING.name())
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inquiries[0].title").value("문의 제목"))
                .andExpect(jsonPath("$.currentPage").value(1));

        verify(adminInquiryService).getAllInquiries(eq(100L), eq(InquiryStatus.PENDING), eq(1), eq(10));
    }

    @Test
    @DisplayName("200 성공: 미답변 문의 목록 조회")
    void getPendingInquiries_Success() throws Exception {
        AdminInquiryListResponse response = AdminInquiryListResponse.builder()
                .id(2L)
                .userId(1L)
                .userEmail("user@test.com")
                .userName("사용자")
                .title("미답변 문의")
                .inquiryCategory(InquiryCategory.PAYMENT)
                .status(InquiryStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .hasAnswer(false)
                .build();

        given(adminInquiryService.getPendingInquiries(100L)).willReturn(List.of(response));

        mockMvc.perform(get("/api/admin/inquiries/pending")
                        .with(authentication(new TestingAuthenticationToken(100L, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("미답변 문의"));

        verify(adminInquiryService).getPendingInquiries(100L);
    }

    @Test
    @DisplayName("200 성공: 관리자 문의 상세 조회")
    void getInquiryDetail_Success() throws Exception {
        InquiryDetailResponse response = InquiryDetailResponse.builder()
                .id(5L)
                .userId(2L)
                .title("관리자 상세")
                .inquiryCategory(InquiryCategory.ETC)
                .content("상세")
                .status(InquiryStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(adminInquiryService.getInquiryDetail(100L, 5L)).willReturn(response);

        mockMvc.perform(get("/api/admin/inquiries/{id}", 5L)
                        .with(authentication(new TestingAuthenticationToken(100L, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("관리자 상세"));

        verify(adminInquiryService).getInquiryDetail(100L, 5L);
    }

    @Test
    @DisplayName("201 성공: 관리자 답변 등록")
    void answerInquiry_Success() throws Exception {
        InquiryDetailResponse response = InquiryDetailResponse.builder()
                .id(7L)
                .userId(3L)
                .title("답변 대상")
                .inquiryCategory(InquiryCategory.ACCOUNT)
                .content("내용")
                .status(InquiryStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(adminInquiryService.answerInquiry(eq(100L), eq(7L), any(CreateAnswerRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/admin/inquiries/{id}/answer", 7L)
                        .with(authentication(new TestingAuthenticationToken(100L, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAnswerRequest("답변"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(InquiryStatus.COMPLETED.name()));

        verify(adminInquiryService).answerInquiry(eq(100L), eq(7L), any(CreateAnswerRequest.class));
    }

    @Test
    @DisplayName("204 성공: 관리자 답변 삭제")
    void deleteAnswer_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/inquiries/{id}/answer", 8L)
                        .with(authentication(new TestingAuthenticationToken(100L, null))))
                .andExpect(status().isNoContent());

        verify(adminInquiryService).deleteAnswer(100L, 8L);
    }

    @Test
    @DisplayName("204 성공: 관리자 문의 삭제")
    void deleteInquiry_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/inquiries/{id}", 9L)
                        .with(authentication(new TestingAuthenticationToken(100L, null))))
                .andExpect(status().isNoContent());

        verify(adminInquiryService).deleteInquiry(100L, 9L);
    }
}
