package vn.com.atomi.charge.notice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.notice.model.enums.NoticeDeliveryLogStatus;
import vn.com.atomi.charge.notice.model.enums.NoticeProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_notice_delivery_logs")
public class NoticeDeliveryLogEntity extends BaseEntity {

    @Column(name = "notice_id", nullable = false)
    private String noticeId;

    @Column(name = "recipient_id")
    private String recipientId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "fcm_token", columnDefinition = "TEXT")
    private String fcmToken;

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private NoticeProvider provider;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private NoticeDeliveryLogStatus status;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

}
