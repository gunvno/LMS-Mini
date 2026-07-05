package vn.com.atomi.charge.authn.model.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.authn.model.enums.RefreshTokenStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_refresh_tokens")
public class RefreshTokenEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "token_id", nullable = false)
    private String tokenId;

    @Column(name = "refresh_token_hash", nullable = false)
    private String refreshTokenHash;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "replaced_by_token_id")
    private String replacedByTokenId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RefreshTokenStatus status;
}
