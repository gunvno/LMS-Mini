package vn.com.atomi.charge.authn.model.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.authn.model.enums.AuthnUserStatus;
import vn.com.atomi.charge.authn.model.enums.UserLanguage;
import vn.com.atomi.charge.base.model.dto.BaseDto;

import java.time.LocalDateTime;
@Getter
@Setter

public class AuthnUserDto extends BaseDto<String> {
    private String username;
    private String passwordHash;
    private String email;
    private String phone;
    private String fullName;
    @Enumerated(EnumType.STRING)
    private UserLanguage language;

    @Enumerated(EnumType.STRING)
    private AuthnUserStatus status;

    private String currentAccessTokenId;

    private LocalDateTime accessTokenExpiredAt;

    private Integer failedLoginAttempts;

    private LocalDateTime accountLockedUntil;

    private LocalDateTime lastLoginAt;

    private LocalDateTime passwordChangeAt;

    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;
}
