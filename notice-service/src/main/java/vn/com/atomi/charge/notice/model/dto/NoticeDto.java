package vn.com.atomi.charge.notice.model.dto;

import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.notice.model.enums.NoticeDeliveryStatus;
import vn.com.atomi.charge.notice.model.enums.NoticeReadStatus;
import vn.com.atomi.charge.notice.model.enums.NoticeStatus;
import vn.com.atomi.charge.notice.model.enums.NoticeTargetType;
import vn.com.atomi.charge.notice.model.enums.NoticeType;

import java.time.LocalDateTime;

@Getter
@Setter
public class NoticeDto {

    private String noticeId;

    private String recipientId;

    private String userId;

    private String title;

    private String content;

    private NoticeType noticeType;

    private NoticeTargetType targetType;

    private String data;

    private NoticeStatus status;

    private NoticeDeliveryStatus deliveryStatus;

    private NoticeReadStatus readStatus;

    private LocalDateTime sentAt;

    private LocalDateTime readAt;
}
