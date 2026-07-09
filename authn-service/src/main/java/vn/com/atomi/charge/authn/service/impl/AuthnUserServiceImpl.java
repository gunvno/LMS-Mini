package vn.com.atomi.charge.authn.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.authn.mapper.AuthnUserMapper;
import vn.com.atomi.charge.authn.model.dto.AuthnUserDto;
import vn.com.atomi.charge.authn.model.entity.AuthnUserEntity;
import vn.com.atomi.charge.authn.model.enums.AuthnUserStatus;
import vn.com.atomi.charge.authn.model.enums.UserLanguage;
import vn.com.atomi.charge.authn.model.request.InternalCreateUserRequest;
import vn.com.atomi.charge.authn.model.request.InternalResetPasswordRequest;
import vn.com.atomi.charge.authn.model.request.InternalUpdateUserStatusRequest;
import vn.com.atomi.charge.authn.repository.AuthnUserRepository;
import vn.com.atomi.charge.authn.service.interfaces.AuthnUserService;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.BaseService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuthnUserServiceImpl extends BaseService<AuthnUserRepository, AuthnUserDto, AuthnUserEntity, AuthnUserMapper>
        implements AuthnUserService {

    private static final String DEFAULT_STAFF_PASSWORD = "123456";

    private final PasswordEncoder passwordEncoder;

    public AuthnUserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Boolean checkUser(String userId) {
        if (!StringUtils.hasText(userId)) {
            return false;
        }
        Optional<AuthnUserEntity> optionalAuthnUser = repository.findEntityById(userId);
        return optionalAuthnUser.isPresent();
    }

    @Override
    public BaseResponse<AuthnUserDto> getUserById(String userId) {
        response = new BaseResponse<>();
        Optional<AuthnUserEntity> optionalUser = repository.findEntityById(userId);
        if (optionalUser.isEmpty()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("user.not_found"));
        }
        AuthnUserEntity entity = optionalUser.get();
        response.setData(mapper.toDto(entity));
        response.setStatus(HttpStatus.OK);
        return response;
    }

    @Override
    public BaseResponse<AuthnUserDto> getUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "user.invalid_request");
        }

        Optional<AuthnUserEntity> optionalUser = repository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("user.not_found"));
        }

        return BaseResponse.success(HttpStatus.OK, mapper.toDto(optionalUser.get()));
    }

    @Override
    public BaseResponse<List<AuthnUserDto>> getUsersByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return BaseResponse.success(HttpStatus.OK, List.of());
        }
        List<AuthnUserDto> users = repository.getAllByIdIn(userIds).stream()
                .map(mapper::toDto)
                .toList();
        return BaseResponse.success(HttpStatus.OK, users);
    }

    @Override
    public BaseResponse<AuthnUserDto> createStaffUser(InternalCreateUserRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getUsername())
                || !StringUtils.hasText(request.getEmail())
                || !StringUtils.hasText(request.getFirstName())
                || !StringUtils.hasText(request.getLastName())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "user.invalid_request");
        }

        if (repository.existsByUsername(request.getUsername())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "user.username_exists");
        }

        if (repository.existsByEmail(request.getEmail())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "user.email_exists");
        }

        LocalDateTime now = LocalDateTime.now();
        String password = StringUtils.hasText(request.getPassword())
                ? request.getPassword()
                : DEFAULT_STAFF_PASSWORD;

        AuthnUserEntity user = new AuthnUserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setFullName(request.getFirstName().trim() + " " + request.getLastName().trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setLanguage(UserLanguage.VI);
        user.setStatus(AuthnUserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setPasswordChangeAt(now);
        user.setCreatedBy("system");
        user.setCreatedDate(now);
        user.setLastModifiedBy("system");
        user.setLastModifiedDate(now);

        AuthnUserEntity saved = repository.save(user);
        return BaseResponse.success(HttpStatus.OK, mapper.toDto(saved));
    }

    @Override
    public BaseResponse<AuthnUserDto> updateUserStatus(String userId, InternalUpdateUserStatusRequest request) {
        if (!StringUtils.hasText(userId) || request == null || !StringUtils.hasText(request.getStatus())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "user.invalid_request");
        }

        Optional<AuthnUserEntity> optionalUser = repository.findEntityById(userId);
        if (optionalUser.isEmpty()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("user.not_found"));
        }

        AuthnUserStatus status = AuthnUserStatus.valueOf(request.getStatus().trim().toUpperCase());
        AuthnUserEntity user = optionalUser.get();
        user.setStatus(status);
        user.setLastModifiedBy("system");
        user.setLastModifiedDate(LocalDateTime.now());
        return BaseResponse.success(HttpStatus.OK, mapper.toDto(repository.save(user)));
    }

    @Override
    public BaseResponse<AuthnUserDto> resetPassword(String userId, InternalResetPasswordRequest request) {
        if (!StringUtils.hasText(userId)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "user.invalid_request");
        }

        Optional<AuthnUserEntity> optionalUser = repository.findEntityById(userId);
        if (optionalUser.isEmpty()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("user.not_found"));
        }

        String password = request != null && StringUtils.hasText(request.getPassword())
                ? request.getPassword()
                : DEFAULT_STAFF_PASSWORD;

        AuthnUserEntity user = optionalUser.get();
        LocalDateTime now = LocalDateTime.now();
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPasswordChangeAt(now);
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastModifiedBy("system");
        user.setLastModifiedDate(now);
        return BaseResponse.success(HttpStatus.OK, mapper.toDto(repository.save(user)));
    }
}
