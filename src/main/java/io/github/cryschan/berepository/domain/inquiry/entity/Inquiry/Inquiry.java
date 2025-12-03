package io.github.cryschan.berepository.domain.inquiry.entity.Inquiry;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "inquiries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id; //문의 id

    @Column(name = "user_id", nullable = false)
    private Long userId; //문의 작성자

    @Column(nullable = false, length = 200)
    private String title; // 사용자 제목

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryCategory inquiryCategory; //문의 카테고리

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; //문의 내용

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InquiryStatus status = InquiryStatus.PENDING; //상태 = default: PENDING(답변 전)

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; //문의 생성일

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; //문의 수정일

    @Builder
    public Inquiry(Long id, Long userId, String title, InquiryCategory inquiryCategory, String content) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.inquiryCategory = inquiryCategory;
        this.content = content;
    }

    //비즈니스 로직

    //상태 변화 - 답변 완료 시 complete 메서드 호출
    public void complete(){
        this.status = InquiryStatus.COMPLETED; //답변 완료 시 상태 변화
    }

    //문의 내용
    public void updateContent(String title, String content){
        this.title = title;
        this.content = content;
    }





















}
