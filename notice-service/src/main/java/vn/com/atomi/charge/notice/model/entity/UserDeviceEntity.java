package vn.com.atomi.charge.notice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.notice.model.enums.DeviceType;
import vn.com.atomi.charge.notice.model.enums.UserDeviceStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_user_devices")
public class UserDeviceEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "device_type")
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    // Keep the legacy physical column so existing databases need no migration.
    @Column(name = "fcm_token", nullable = false, columnDefinition = "TEXT")
    private String installationId;

    @Column(name = "app_version")
    private String appVersion;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserDeviceStatus status;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

}
