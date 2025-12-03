package io.github.cryschan.berepository.domain.inquiry.entity.Answer;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 문의 답변 엔티티
 *
 * ⭐ 중요: adminUserId는 "누가 답변했는지"만 저장합니다.
 * - User가 관리자인지 확인하는 것은 Service에서 처리!
 * - Entity는 단순히 데이터만 저장하는 역할
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "inquiry_answers")
@EntityListeners(AuditingEntityListener.class)
public class InquiryAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inquiry_id", nullable = false, unique = true)
    private Long inquiryId;

    /**
     * 답변 작성자 (관리자) ID
     *
     * ⭐ 중요: 이 필드는 단순히 ID만 저장합니다!
     * - "이 사람이 관리자인가?"는 Service에서 검증
     * - 여기서는 검증 없이 저장만 함
     */
    @Column(name = "admin_user_id", nullable = false)
    private Long adminUserId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answerContent;

    private LocalDateTime answeredAt;

    @Builder
    public InquiryAnswer(Long inquiryId, Long adminUserId, String answerContent, LocalDateTime answeredAt) {
        this.inquiryId = inquiryId;
        this.adminUserId = adminUserId;
        this.answerContent = answerContent;
        this.answeredAt = answeredAt;
    }
}
