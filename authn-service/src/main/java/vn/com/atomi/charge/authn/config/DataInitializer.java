package vn.com.atomi.charge.authn.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
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

    private final AuthnUserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-demo-users:false}")
    private boolean seedDemoUsers;

    @Value("${app.demo-user-password:}")
    private String demoUserPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedDemoUsers || demoUserPassword.isBlank()) {
            return;
        }
        ensureDefaultUser("admin", "admin@gmail.com", "System Admin");
        ensureDefaultUser("instructor", "instructor@gmail.com", "Default Instructor");
        ensureDefaultUser("student", "student@gmail.com", "Default Student");
    }

    private void ensureDefaultUser(String username, String email, String fullName) {
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        AuthnUserEntity user = new AuthnUserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(demoUserPassword));
        user.setLanguage(UserLanguage.VI);
        user.setStatus(AuthnUserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setPasswordChangeAt(now);
        user.setCreatedBy("system");
        user.setCreatedDate(now);
        user.setLastModifiedBy("system");
        user.setLastModifiedDate(now);

        AuthnUserEntity saved = userRepository.save(user);

        log.warn("Created demo LMS account. id={}, username={}, email={}",
                saved.getId(), username, email);
    }
}
