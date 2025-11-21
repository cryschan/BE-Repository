package io.github.cryschan.berepository.domain.user.service;

import io.github.cryschan.berepository.domain.user.dto.response.UserResponse;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// 인증된 사용자만 작업을 수행할 수 있다.
@RequiredArgsConstructor
@Service
public class UserSupportService {

    private final UserRepository userRepository;

    // 유저 아이디 조회
    // 외부 도메인에서 유저를 찾을 경우 사용한다.
    public UserResponse findById(Long userId) {

        // userId 검증

        // 유저 아이디로 리포지토리 조회

        // entity -> dto
        return null;
    }
}
