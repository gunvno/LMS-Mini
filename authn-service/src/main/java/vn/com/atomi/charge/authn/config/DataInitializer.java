package vn.com.atomi.charge.authn.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.com.atomi.charge.authn.model.enums.AuthnUserStatus;
import vn.com.atomi.charge.authn.model.enums.UserLanguage;
import vn.com.atomi.charge.authn.repository.AuthnUserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "123456";

    private final AuthnUserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureDefaultUser(
                "00000000-0000-0000-0000-000000000001",
                "admin",
                "admin@gmail.com",
                "System Admin"
        );
        ensureDefaultUser(
                "00000000-0000-0000-0000-000000000002",
                "instructor",
                "instructor@gmail.com",
                "Default Instructor"
        );
        ensureDefaultUser(
                "00000000-0000-0000-0000-000000000003",
                "student",
                "student@gmail.com",
                "Default Student"
        );
    }

    private void ensureDefaultUser(String id, String username, String email, String fullName) {
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("""
                        INSERT INTO tbl_users (
                            id, version, created_by, created_date, last_modified_by, last_modified_date,
                            username, password_hash, email, full_name, language, status,
                            failed_login_attempts, password_change_at
                        ) VALUES (?, 0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?)
                        """,
                id,
                "system",
                Timestamp.valueOf(now),
                "system",
                Timestamp.valueOf(now),
                username,
                passwordEncoder.encode(DEFAULT_PASSWORD),
                email,
                fullName,
                UserLanguage.VI.name(),
                AuthnUserStatus.ACTIVE.name(),
                Timestamp.valueOf(now));

        log.warn("Created default LMS account. username={}, email={}, password={}", username, email, DEFAULT_PASSWORD);
    }
}
