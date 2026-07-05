package vn.com.atomi.charge.authn.model.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.authn.model.enums.AuthnUserStatus;
import vn.com.atomi.charge.authn.model.enums.UserLanguage;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_users")
public class AuthnUserEntity extends BaseEntity {

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "language")
    @Enumerated(EnumType.STRING)
    private UserLanguage language;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthnUserStatus status;

    @Column(name = "current_access_token_id")
    private String currentAccessTokenId;

    @Column(name = "access_token_expired_at")
    private LocalDateTime accessTokenExpiredAt;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_change_at")
    private LocalDateTime passwordChangeAt;
}
