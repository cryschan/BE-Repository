package io.github.cryschan.berepository.domain.inquiry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.cryschan.berepository._global.exception.handler.GlobalExceptionHandler;
import io.github.cryschan.berepository.domain.inquiry.dto.Request.CreateInquiryRequest;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryDetailResponse;
import io.github.cryschan.berepository.domain.inquiry.dto.Response.InquiryListResponse;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryCategory;
import io.github.cryschan.berepository.domain.inquiry.entity.Inquiry.InquiryStatus;
import io.github.cryschan.berepository.domain.inquiry.service.InquiryService;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InquiryController 테스트")
class InquiryControllerTest {

    @Mock
    private InquiryService inquiryService;

    @InjectMocks
    private InquiryController inquiryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(inquiryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("201 성공: 문의를 생성한다")
    void createInquiry_Success() throws Exception {
        // given
        CreateInquiryRequest request = new CreateInquiryRequest("제목", InquiryCategory.FEATURE, "내용");
        InquiryDetailResponse response = InquiryDetailResponse.builder()
                .id(1L)
                .userId(1L)
                .title("제목")
                .inquiryCategory(InquiryCategory.FEATURE)
                .content("내용")
                .status(InquiryStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(inquiryService.createInquiry(eq(1L), any(CreateInquiryRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/inquiries")
                        .with(authentication(new TestingAuthenticationToken(1L, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.userId").value(1L));

        verify(inquiryService).createInquiry(eq(1L), any(CreateInquiryRequest.class));
    }

    @Test
    @DisplayName("200 성공: 내 문의 목록을 조회한다")
    void getMyInquiries_Success() throws Exception {
        // given
        InquiryListResponse listResponse = InquiryListResponse.builder()
                .id(10L)
                .title("문의1")
                .inquiryCategory(InquiryCategory.PAYMENT)
                .status(InquiryStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .hasAnswer(false)
                .build();
        Page<InquiryListResponse> page = new PageImpl<>(List.of(listResponse), PageRequest.of(0, 10), 1);

        given(inquiryService.getMyInquiries(eq(1L), eq(1), isNull())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/inquiries")
                        .with(authentication(new TestingAuthenticationToken(1L, null)))
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inquiries[0].title").value("문의1"))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(inquiryService).getMyInquiries(eq(1L), eq(1), isNull());
    }

    @Test
    @DisplayName("200 성공: 상태별 문의 목록을 조회한다")
    void getMyInquiries_FilterByStatus() throws Exception {
        // given
        InquiryListResponse listResponse = InquiryListResponse.builder()
                .id(20L)
                .title("완료된 문의")
                .inquiryCategory(InquiryCategory.FEATURE)
                .status(InquiryStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .hasAnswer(true)
                .build();
        Page<InquiryListResponse> page = new PageImpl<>(List.of(listResponse), PageRequest.of(0, 10), 1);

        given(inquiryService.getMyInquiries(1L, 1, InquiryStatus.COMPLETED)).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/inquiries")
                        .with(authentication(new TestingAuthenticationToken(1L, null)))
                        .param("page", "1")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inquiries[0].status").value("COMPLETED"));

        verify(inquiryService).getMyInquiries(1L, 1, InquiryStatus.COMPLETED);
    }

    @Test
    @DisplayName("200 성공: 문의 상세를 조회한다")
    void getInquiryDetail_Success() throws Exception {
        // given
        InquiryDetailResponse response = InquiryDetailResponse.builder()
                .id(5L)
                .userId(1L)
                .title("상세 문의")
                .inquiryCategory(InquiryCategory.ACCOUNT)
                .content("상세 내용")
                .status(InquiryStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(inquiryService.getInquiryDetail(1L, 5L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/inquiries/{id}", 5L)
                        .with(authentication(new TestingAuthenticationToken(1L, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.title").value("상세 문의"));

        verify(inquiryService).getInquiryDetail(1L, 5L);
    }
}
