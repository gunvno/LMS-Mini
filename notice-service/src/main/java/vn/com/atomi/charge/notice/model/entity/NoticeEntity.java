package vn.com.atomi.charge.notice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.notice.model.enums.NoticeStatus;
import vn.com.atomi.charge.notice.model.enums.NoticeTargetType;
import vn.com.atomi.charge.notice.model.enums.NoticeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_notices")
public class NoticeEntity extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "notice_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NoticeType noticeType;

    @Column(name = "target_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NoticeTargetType targetType;

    @Column(name = "data", columnDefinition = "TEXT")
    private String data;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private NoticeStatus status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

}
