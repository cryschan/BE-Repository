package io.github.cryschan.berepository._global;

import io.github.cryschan.berepository.domain.user.entity.User;
import io.github.cryschan.berepository.domain.user.entity.role.UserRole;
import io.github.cryschan.berepository.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 시작 시 초기 사용자 데이터를 생성하는 클래스
 * dev, local 프로필에서만 실행됩니다.
 */
@Slf4j
@RequiredArgsConstructor
@Order(1)
@Component
public class UserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${init.admin.email}")
    private String adminEmail;

    @Value("${init.admin.password}")
    private String adminPassword;

    @Value("${init.admin.username}")
    private String adminUsername;

    @Value("${init.admin.department}")
    private String adminDepartment;

    @Value("${init.user.email}")
    private String userEmail;

    @Value("${init.user.password}")
    private String userPassword;

    @Value("${init.user.username}")
    private String userUsername;

    @Value("${init.user.department}")
    private String userDepartment;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=".repeat(80));
        log.info("Starting Initial User Data Initialization...");
        log.info("=".repeat(80));

        try {
            createAdminIfNotExists();
            createUserIfNotExists();

            log.info("=".repeat(80));
            log.info("Initial User Data Initialization Completed Successfully");
            log.info("=".repeat(80));
        } catch (Exception e) {
            log.error("=".repeat(80));
            log.error("Initial User Data Initialization Failed!", e);
            log.error("=".repeat(80));
            throw e;
        }
    }

    private void createAdminIfNotExists() {
        log.debug("Checking if Admin user exists...");
        log.debug("Admin email to check: {}", adminEmail);

        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("[SKIP] Admin user already exists: {}", adminEmail);
            log.debug("Admin user creation skipped - user already in database");
            return;
        }

        log.debug("Admin user does not exist. Creating new Admin user...");
        log.debug("Admin email: {}", adminEmail);
        log.debug("Admin username: {}", adminUsername);
        log.debug("Admin department: {}", adminDepartment);
        log.debug("Admin role: {}", UserRole.ADMIN);

        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .username(adminUsername)
                .role(UserRole.ADMIN)
                .department(adminDepartment)
                .build();

        User savedAdmin = userRepository.save(admin);

        log.info("[CREATED] Admin user successfully created");
        log.debug("  - User ID: {}", savedAdmin.getUserId());
        log.debug("  - Email: {}", savedAdmin.getEmail());
        log.debug("  - Username: {}", savedAdmin.getUsername());
        log.debug("  - Role: {}", savedAdmin.getRole());
        log.debug("  - Department: {}", savedAdmin.getDepartment());
        log.debug("  - Password encrypted: {}", savedAdmin.getPassword().startsWith("$2"));
    }

    private void createUserIfNotExists() {
        log.debug("Checking if regular User exists...");
        log.debug("User email to check: {}", userEmail);

        if (userRepository.findByEmail(userEmail).isPresent()) {
            log.info("[SKIP] Regular user already exists: {}", userEmail);
            log.debug("Regular user creation skipped - user already in database");
            return;
        }

        log.debug("Regular user does not exist. Creating new User...");
        log.debug("User email: {}", userEmail);
        log.debug("User username: {}", userUsername);
        log.debug("User department: {}", userDepartment);
        log.debug("User role: {}", UserRole.USER);

        User user = User.builder()
                .email(userEmail)
                .password(passwordEncoder.encode(userPassword))
                .username(userUsername)
                .role(UserRole.USER)
                .department(userDepartment)
                .build();

        User savedUser = userRepository.save(user);

        log.info("[CREATED] Regular user successfully created");
        log.debug("  - User ID: {}", savedUser.getUserId());
        log.debug("  - Email: {}", savedUser.getEmail());
        log.debug("  - Username: {}", savedUser.getUsername());
        log.debug("  - Role: {}", savedUser.getRole());
        log.debug("  - Department: {}", savedUser.getDepartment());
        log.debug("  - Password encrypted: {}", savedUser.getPassword().startsWith("$2"));
    }
}
