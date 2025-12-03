package io.github.cryschan.berepository.domain.user.repository;

import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Integer countByRole(UserRole role);

    @Query("SELECT u.role FROM User u WHERE u.userId = :userId")
    Optional<UserRole> findRoleById(@Param("userId") Long userId);
}
