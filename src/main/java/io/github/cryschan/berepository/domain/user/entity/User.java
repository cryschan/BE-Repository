package io.github.cryschan.berepository.domain.user.entity;

import io.github.cryschan.berepository.domain.user.dto.request.LoginRequest;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Table(name = "users")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class User {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long userId; // 유저 아이디

    @Column(unique = true)
    private String email; // 유저 이메일 (고유해야함)
    private String password;
    private String username;
    private UserRole role;
    private String department; // 부서
    private Long tokenUsage;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public User(String email, String password, String username, UserRole role, String department) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.role = role != null ? role : UserRole.USER; // 만약 입력이 없으면 default로 USER 설정한다.
        this.department = department;
    }

    public static User form(LoginRequest loginRequest) {
        return User.builder()
                .email(loginRequest.email())
                .password(loginRequest.password())
                .username(loginRequest.username())
                .department(loginRequest.department())
                .role(UserRole.USER)  // 기본 역할 설정
                .build();
    }

    public static User form(LoginRequest loginRequest, String encodedPassword) {
        return User.builder()
                .email(loginRequest.email())
                .password(encodedPassword)  // 암호화된 비밀번호 사용
                .username(loginRequest.username())
                .department(loginRequest.department())
                .role(UserRole.USER)
                .build();
    }
}
