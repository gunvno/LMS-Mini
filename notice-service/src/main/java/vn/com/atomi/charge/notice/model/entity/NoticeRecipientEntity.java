package vn.com.atomi.charge.notice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.notice.model.enums.NoticeDeliveryStatus;
import vn.com.atomi.charge.notice.model.enums.NoticeReadStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_notice_recipients")
public class NoticeRecipientEntity extends BaseEntity {

    @Column(name = "notice_id", nullable = false)
    private String noticeId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "delivery_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private NoticeDeliveryStatus deliveryStatus;

    @Column(name = "read_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private NoticeReadStatus readStatus;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

}
