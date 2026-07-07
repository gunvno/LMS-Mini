package vn.com.atomi.charge.authn.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.com.atomi.charge.authn.model.entity.AuthnUserEntity;
import vn.com.atomi.charge.authn.model.enums.AuthnUserStatus;
import vn.com.atomi.charge.authn.model.enums.UserLanguage;
import vn.com.atomi.charge.authn.repository.AuthnUserRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "123456";

    private final AuthnUserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

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
        AuthnUserEntity user = new AuthnUserEntity();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setLanguage(UserLanguage.VI);
        user.setStatus(AuthnUserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setPasswordChangeAt(now);
        user.setCreatedBy("system");
        user.setCreatedDate(now);
        user.setLastModifiedBy("system");
        user.setLastModifiedDate(now);
        userRepository.save(user);

        log.warn("Created default LMS account. username={}, email={}, password={}", username, email, DEFAULT_PASSWORD);
    }
}
