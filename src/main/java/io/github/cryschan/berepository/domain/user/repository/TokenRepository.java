package io.github.cryschan.berepository.domain.user.repository;

import io.github.cryschan.berepository.domain.user.entity.RefreshToken;
import io.github.cryschan.berepository.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Refresh Token 저장소 인터페이스
 * 리프레시 토큰의 CRUD 및 조회 기능을 제공합니다.
 */
public interface TokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.token = :token")
    void deleteByToken(@Param("token") String token);
}
