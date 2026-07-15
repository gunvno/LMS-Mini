package vn.com.atomi.charge.authn.service.impl;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import vn.com.atomi.charge.authn.client.AuthorClient;
import vn.com.atomi.charge.authn.model.entity.AuthnUserEntity;
import vn.com.atomi.charge.authn.model.enums.AuthnUserStatus;
import vn.com.atomi.charge.authn.model.enums.ClientPortal;
import vn.com.atomi.charge.authn.model.enums.ErrorCode;
import vn.com.atomi.charge.authn.model.request.AuthenticationRequest;
import vn.com.atomi.charge.authn.model.request.ChangePasswordRequest;
import vn.com.atomi.charge.authn.repository.AuthnRepo;
import vn.com.atomi.charge.authn.repository.AuthnUserRepository;
import vn.com.atomi.charge.authn.service.interfaces.MailService;
import vn.com.atomi.charge.base.i18n.YamlMessageSource;
import vn.com.atomi.charge.base.model.exception.BusinessException;
import vn.com.atomi.charge.base.model.response.BaseResponse;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthnServiceImplTest {

    private static final String SIGNER_KEY =
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Mock
    private AuthnUserRepository userRepository;
    @Mock
    private AuthnRepo refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate;
    @Mock
    private MailService mailService;
    @Mock
    private AuthorClient authorClient;

    private AuthnServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthnServiceImpl(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                redisTemplate,
                mailService,
                authorClient);
        ReflectionTestUtils.setField(service, "signerKey", SIGNER_KEY);
        ReflectionTestUtils.setField(service, "validDuration", 900L);
        ReflectionTestUtils.setField(service, "refreshableDuration", 3600L);
    }

    @Test
    void invalidOldPasswordUsesConfiguredI18nKey() throws Exception {
        AuthnUserEntity user = user("old-hash");
        when(refreshTokenRepository.existsByTokenId("token-id")).thenReturn(false);
        when(userRepository.findEntityById("user-1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "old-hash")).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.changePassword(
                        token(ClientPortal.STUDENT),
                        changeRequest("wrong-password", "new-password", "new-password"),
                        ClientPortal.STUDENT));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_OLD_PASSWORD.getErrorCode());
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.INVALID_OLD_PASSWORD.getMessageKey());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void successfulPasswordChangePersistsTheEncodedPassword() throws Exception {
        AuthnUserEntity user = user("old-hash");
        when(refreshTokenRepository.existsByTokenId("token-id")).thenReturn(false);
        when(userRepository.findEntityById("user-1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-password", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        service.changePassword(
                token(ClientPortal.STUDENT),
                changeRequest("old-password", "new-password", "new-password"),
                ClientPortal.STUDENT);

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(user.getPasswordChangeAt()).isNotNull();
        verify(userRepository).saveAndFlush(user);
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void everyAuthenticationMessageKeyHasAllSupportedTranslations() {
        YamlMessageSource messages = new YamlMessageSource("i18n/messages");
        List<String> keys = new java.util.ArrayList<>();
        for (ErrorCode errorCode : ErrorCode.values()) {
            keys.add(errorCode.getMessageKey());
        }
        keys.addAll(List.of(
                "auth.username_existed",
                "auth.email_existed",
                "auth.otp_required",
                "auth.otp_invalid",
                "auth.otp_email_send_failed",
                "auth.invalid_key",
                "auth.portal_required",
                "auth.student_portal_forbidden",
                "auth.admin_portal_forbidden",
                "auth.portal_access_check_failed",
                "security.invalid_token"));

        for (Locale locale : List.of(Locale.forLanguageTag("vi"), Locale.ENGLISH, Locale.forLanguageTag("lo"))) {
            for (String key : keys) {
                assertThat(messages.getMessage(key, null, locale))
                        .as("translation for %s in %s", key, locale)
                        .isNotEqualTo(key);
            }
        }
    }

    @Test
    void studentAccountCannotCreateAnAdminSession() {
        AuthnUserEntity user = activeUser("student", "password-hash");
        when(userRepository.findByUsernameOrEmail("student", "student")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "password-hash")).thenReturn(true);
        when(authorClient.getUserRoles("user-1"))
                .thenReturn(BaseResponse.success(HttpStatus.OK, List.of("STUDENT")));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.authenticate(loginRequest("student", "password"), ClientPortal.ADMIN));

        assertThat(exception.getCode()).isEqualTo("FORBIDDEN");
        assertThat(exception.getMessage()).isEqualTo("auth.admin_portal_forbidden");
        verify(userRepository, never()).save(any());
    }

    @Test
    void adminAccountCannotCreateAStudentSession() {
        AuthnUserEntity user = activeUser("admin", "password-hash");
        when(userRepository.findByUsernameOrEmail("admin", "admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "password-hash")).thenReturn(true);
        when(authorClient.getUserRoles("user-1"))
                .thenReturn(BaseResponse.success(HttpStatus.OK, List.of("ADMIN")));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.authenticate(loginRequest("admin", "password"), ClientPortal.STUDENT));

        assertThat(exception.getCode()).isEqualTo("FORBIDDEN");
        assertThat(exception.getMessage()).isEqualTo("auth.student_portal_forbidden");
        verify(userRepository, never()).save(any());
    }

    private AuthnUserEntity user(String passwordHash) {
        AuthnUserEntity user = new AuthnUserEntity();
        user.setId("user-1");
        user.setUsername("student");
        user.setPasswordHash(passwordHash);
        return user;
    }

    private AuthnUserEntity activeUser(String username, String passwordHash) {
        AuthnUserEntity user = user(passwordHash);
        user.setUsername(username);
        user.setStatus(AuthnUserStatus.ACTIVE);
        user.setEmail(username + "@example.com");
        user.setFullName(username);
        return user;
    }

    private AuthenticationRequest loginRequest(String username, String password) {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private ChangePasswordRequest changeRequest(String oldPassword, String newPassword, String confirmPassword) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword(oldPassword);
        request.setNewPassword(newPassword);
        request.setConfirmPassword(confirmPassword);
        return request;
    }

    private String token(ClientPortal portal) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("user-1")
                .jwtID("token-id")
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .claim("portal", portal.name())
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
        jwt.sign(new MACSigner(SIGNER_KEY.getBytes()));
        return jwt.serialize();
    }
}
