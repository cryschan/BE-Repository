package io.github.cryschan.berepository.domain.user.service;

import io.github.cryschan.berepository.domain.user.dto.request.UpdateProfileRequest;
import io.github.cryschan.berepository.domain.user.dto.response.UserDetailResponse;
import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import io.github.cryschan.berepository.domain.user.exception.UserException;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 프로필 서비스
 * 마이페이지 조회, 수정 등 프로필 관련 비즈니스 로직을 처리합니다.
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserProfileService {

    private final UserRepository userRepository;

    // 자기 프로필 조회
    public UserDetailResponse getMyProfile(Long userId) {

        // 유저 아이디가 null인지 확인
        if (userId == null) {
            throw UserException.invalidInput("userId cannot be null");
        }

        // 데이터 베이스 조회 -> 존재하지 않으면 예외
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound(userId));

        return UserDetailResponse.from(user);
    }

    // 다른 사용자 조회 (현재는 관리자만 다른 사용자 조회 가능)
    public UserDetailResponse getUserProfile(Long userId, Long targetUserId) {

        // 관리자 아이디 찾기
        User admin = userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound(userId));

        // 만약 amin이 아니라면 예외 발생
        if (admin.getRole() != UserRole.ADMIN) {
            throw UserException.accessDenied();
        }

        // 조회할 유저 프로필
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> UserException.notFound(targetUserId));

        return UserDetailResponse.from(targetUser);
    }

    // 사용자 프로필 업데이트
    @Transactional
    public UserDetailResponse updateMyProfile(Long userId, UpdateProfileRequest request) {

        // userId가 null인경우
        if (userId == null) {
            throw UserException.notFound("userId cannot be null");
        }

        // 유저 조회
        User updateUser = userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound(userId));

        // 유저 데이터 업데이트
        updateUser.updateProfile(request.username(), request.department());

        // entity -> dto
        return UserDetailResponse.from(updateUser);
    }
}
