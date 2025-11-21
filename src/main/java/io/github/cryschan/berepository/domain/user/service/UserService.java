package io.github.cryschan.berepository.domain.user.service;

import io.github.cryschan.berepository.domain.user.dto.request.LoginRequest;
import io.github.cryschan.berepository.domain.user.dto.response.UserResponse;
import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.exception.DuplicationUserException;
import io.github.cryschan.berepository.domain.user.exception.InvalidCredentialsException;
import io.github.cryschan.berepository.domain.user.exception.UserNotFoundException;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public UserResponse signup(LoginRequest loginRequest) {

        // 중복 이메일 체크
        // 로그인시 입력한 이메일이 데이터베이스에 등록된 이메일과 같으면 예외 발생
        // 1. 데이터 베이스에서 loginRequest의 이메일을 조회한다.
        // 2. 조회한 이메일이 존재하면 예외를 발생시킨다.

        if (userRepository.findByEmail(loginRequest.email()).isPresent()) {
            throw new DuplicationUserException(loginRequest.email());
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(loginRequest.password());

        // 유저 생성 (암호화된 비밀번호 전달)
        User newUser = User.form(loginRequest, encodedPassword);

        // 회원 등록
        userRepository.save(newUser);

        // entity -> dto
        return UserResponse.from(newUser);
    }

    // 로그인
    @Transactional
    public UserResponse login(LoginRequest loginRequest) {

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new InvalidCredentialsException(loginRequest.email()));

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new InvalidCredentialsException(loginRequest.email());
        }

        // TODO: JWT 토큰 생성 로직 추가 필요

        // entity -> dto
        return UserResponse.from(user);
    }
}
