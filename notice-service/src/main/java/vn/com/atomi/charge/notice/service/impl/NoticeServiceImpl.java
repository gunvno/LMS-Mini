package vn.com.atomi.charge.notice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.notice.client.AuthorClient;
import vn.com.atomi.charge.notice.model.dto.NoticeDto;
import vn.com.atomi.charge.notice.model.entity.NoticeDeliveryLogEntity;
import vn.com.atomi.charge.notice.model.entity.NoticeEntity;
import vn.com.atomi.charge.notice.model.entity.NoticeRecipientEntity;
import vn.com.atomi.charge.notice.model.entity.UserDeviceEntity;
import vn.com.atomi.charge.notice.model.enums.NoticeDeliveryLogStatus;
import vn.com.atomi.charge.notice.model.enums.NoticeDeliveryStatus;
import vn.com.atomi.charge.notice.model.enums.NoticeProvider;
import vn.com.atomi.charge.notice.model.enums.NoticeReadStatus;
import vn.com.atomi.charge.notice.model.enums.NoticeStatus;
import vn.com.atomi.charge.notice.model.enums.NoticeTargetType;
import vn.com.atomi.charge.notice.model.enums.NoticeType;
import vn.com.atomi.charge.notice.model.request.NoticeSendAllRequest;
import vn.com.atomi.charge.notice.model.request.NoticeSendRoleRequest;
import vn.com.atomi.charge.notice.model.request.NoticeSendUserRequest;
import vn.com.atomi.charge.notice.model.request.NoticeSendUsersRequest;
import vn.com.atomi.charge.notice.repository.NoticeDeliveryLogRepository;
import vn.com.atomi.charge.notice.repository.NoticeRecipientRepository;
import vn.com.atomi.charge.notice.repository.NoticeRepository;
import vn.com.atomi.charge.notice.service.interfaces.DeviceService;
import vn.com.atomi.charge.notice.service.interfaces.FirebasePushService;
import vn.com.atomi.charge.notice.service.interfaces.NoticeService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeRecipientRepository recipientRepository;
    private final NoticeDeliveryLogRepository deliveryLogRepository;
    private final DeviceService deviceService;
    private final FirebasePushService firebasePushService;
    private final AuthorClient authorClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Void> sendUser(BaseRequest<NoticeSendUserRequest> request) {
        NoticeSendUserRequest data = request == null ? null : request.getData();

        if (data == null
                || !StringUtils.hasText(data.getUserId())
                || !StringUtils.hasText(data.getTitle())
                || !StringUtils.hasText(data.getContent())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "notice.invalid_request");
        }

        return sendToUsers(
                List.of(data.getUserId()),
                data.getTitle(),
                data.getContent(),
                data.getNoticeType(),
                data.getData(),
                NoticeTargetType.USER
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Void> sendUsers(BaseRequest<NoticeSendUsersRequest> request) {
        NoticeSendUsersRequest data = request == null ? null : request.getData();

        if (data == null
                || data.getUserIds() == null
                || data.getUserIds().isEmpty()
                || !StringUtils.hasText(data.getTitle())
                || !StringUtils.hasText(data.getContent())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "notice.invalid_request");
        }

        return sendToUsers(
                data.getUserIds(),
                data.getTitle(),
                data.getContent(),
                data.getNoticeType(),
                data.getData(),
                NoticeTargetType.USERS
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Void> sendRole(BaseRequest<NoticeSendRoleRequest> request) {
        NoticeSendRoleRequest data = request == null ? null : request.getData();

        if (data == null
                || !StringUtils.hasText(data.getRoleCode())
                || !StringUtils.hasText(data.getTitle())
                || !StringUtils.hasText(data.getContent())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "notice.invalid_request");
        }

        BaseResponse<List<String>> userResponse = authorClient.getUsersByRole(data.getRoleCode());
        List<String> userIds = userResponse == null || userResponse.getData() == null
                ? List.of()
                : userResponse.getData();

        return sendToUsers(
                userIds,
                data.getTitle(),
                data.getContent(),
                data.getNoticeType(),
                data.getData(),
                NoticeTargetType.ROLE
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Void> sendAll(BaseRequest<NoticeSendAllRequest> request) {
        NoticeSendAllRequest data = request == null ? null : request.getData();

        if (data == null
                || !StringUtils.hasText(data.getTitle())
                || !StringUtils.hasText(data.getContent())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "notice.invalid_request");
        }

        List<String> userIds = deviceService.getAllActiveDevices().stream()
                .map(UserDeviceEntity::getUserId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        return sendToUsers(
                userIds,
                data.getTitle(),
                data.getContent(),
                data.getNoticeType(),
                data.getData(),
                NoticeTargetType.ALL
        );
    }

    @Override
    public BaseResponse<Page<NoticeDto>> getMyNotices(Pageable pageable) {
        String userId = currentUserId();
        if (!StringUtils.hasText(userId)) {
            return BaseResponse.fail(HttpStatus.UNAUTHORIZED, "user.not_found");
        }

        Page<NoticeRecipientEntity> recipients =
                recipientRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedDateDesc(userId, pageable);
        return BaseResponse.success(HttpStatus.OK, recipients.map(this::toDto));
    }

    @Override
    public BaseResponse<Long> getMyUnreadCount() {
        String userId = currentUserId();
        if (!StringUtils.hasText(userId)) {
            return BaseResponse.fail(HttpStatus.UNAUTHORIZED, "user.not_found");
        }

        long count = recipientRepository.countByUserIdAndReadStatusAndDeletedAtIsNull(
                userId,
                NoticeReadStatus.UNREAD);
        return BaseResponse.success(HttpStatus.OK, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Void> markRead(String recipientId) {
        String userId = currentUserId();
        if (!StringUtils.hasText(userId)) {
            return BaseResponse.fail(HttpStatus.UNAUTHORIZED, "user.not_found");
        }
        if (!StringUtils.hasText(recipientId)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "notice.not_found");
        }

        Optional<NoticeRecipientEntity> optionalRecipient =
                recipientRepository.findByIdAndUserIdAndDeletedAtIsNull(recipientId, userId);
        if (optionalRecipient.isEmpty()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "notice.not_found");
        }

        NoticeRecipientEntity recipient = optionalRecipient.get();
        recipient.setReadStatus(NoticeReadStatus.READ);
        recipient.setReadAt(LocalDateTime.now());
        recipientRepository.save(recipient);

        return BaseResponse.success(HttpStatus.OK, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Void> markAllRead() {
        String userId = currentUserId();
        if (!StringUtils.hasText(userId)) {
            return BaseResponse.fail(HttpStatus.UNAUTHORIZED, "user.not_found");
        }

        List<NoticeRecipientEntity> unreadRecipients =
                recipientRepository.findByUserIdAndReadStatusAndDeletedAtIsNull(userId, NoticeReadStatus.UNREAD);
        LocalDateTime now = LocalDateTime.now();
        for (NoticeRecipientEntity recipient : unreadRecipients) {
            recipient.setReadStatus(NoticeReadStatus.READ);
            recipient.setReadAt(now);
        }
        recipientRepository.saveAll(unreadRecipients);

        return BaseResponse.success(HttpStatus.OK, null);
    }

    private BaseResponse<Void> sendToUsers(List<String> rawUserIds,
                                           String title,
                                           String content,
                                           NoticeType noticeType,
                                           String data,
                                           NoticeTargetType targetType) {
        List<String> userIds = normalizeUserIds(rawUserIds);
        if (userIds.isEmpty()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "notice.no_recipient");
        }

        NoticeEntity notice = new NoticeEntity();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setNoticeType(noticeType == null ? NoticeType.SYSTEM : noticeType);
        notice.setTargetType(targetType);
        notice.setData(data);
        notice.setStatus(NoticeStatus.SENDING);
        notice.setSentAt(LocalDateTime.now());
        notice = noticeRepository.save(notice);

        boolean allSuccess = true;

        for (String userId : userIds) {
            NoticeRecipientEntity recipient = createRecipient(notice.getId(), userId);
            List<UserDeviceEntity> devices = deviceService.getActiveDevices(userId);
            boolean recipientSuccess = sendToRecipientDevices(notice, recipient, devices);

            if (!recipientSuccess) {
                allSuccess = false;
            }
        }

        notice.setStatus(allSuccess ? NoticeStatus.SENT : NoticeStatus.FAILED);
        noticeRepository.save(notice);

        return BaseResponse.success(HttpStatus.OK, null);
    }

    private List<String> normalizeUserIds(List<String> rawUserIds) {
        if (rawUserIds == null || rawUserIds.isEmpty()) {
            return List.of();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String userId : rawUserIds) {
            if (StringUtils.hasText(userId)) {
                normalized.add(userId.trim());
            }
        }
        return new ArrayList<>(normalized);
    }

    private NoticeRecipientEntity createRecipient(String noticeId, String userId) {
        NoticeRecipientEntity recipient = new NoticeRecipientEntity();
        recipient.setNoticeId(noticeId);
        recipient.setUserId(userId);
        recipient.setDeliveryStatus(NoticeDeliveryStatus.PENDING);
        recipient.setReadStatus(NoticeReadStatus.UNREAD);
        recipient.setSentAt(LocalDateTime.now());
        return recipientRepository.save(recipient);
    }

    private boolean sendToRecipientDevices(NoticeEntity notice,
                                           NoticeRecipientEntity recipient,
                                           List<UserDeviceEntity> devices) {
        boolean allDevicesSuccess = true;

        for (UserDeviceEntity device : devices) {
            try {
                String providerMessageId = firebasePushService.send(
                        device.getFcmToken(),
                        notice.getTitle(),
                        notice.getContent(),
                        buildFirebaseData(notice.getId(), notice.getNoticeType(), notice.getData())
                );

                saveDeliveryLog(notice.getId(), recipient.getId(), recipient.getUserId(), device,
                        NoticeDeliveryLogStatus.SENT, providerMessageId, null, null);
            } catch (Exception ex) {
                allDevicesSuccess = false;
                saveDeliveryLog(notice.getId(), recipient.getId(), recipient.getUserId(), device,
                        NoticeDeliveryLogStatus.FAILED, null, "FIREBASE_SEND_FAILED", ex.getMessage());
            }
        }

        if (devices.isEmpty() || !allDevicesSuccess) {
            recipient.setDeliveryStatus(NoticeDeliveryStatus.FAILED);
            recipient.setFailureReason(devices.isEmpty() ? "NO_ACTIVE_DEVICE" : "FIREBASE_SEND_FAILED");
        } else {
            recipient.setDeliveryStatus(NoticeDeliveryStatus.SENT);
        }

        recipientRepository.save(recipient);
        return !devices.isEmpty() && allDevicesSuccess;
    }

    private Map<String, String> buildFirebaseData(String noticeId, NoticeType noticeType, String data) {
        Map<String, String> result = new HashMap<>();
        result.put("noticeId", noticeId);
        result.put("noticeType", noticeType == null ? NoticeType.SYSTEM.name() : noticeType.name());

        if (StringUtils.hasText(data)) {
            result.put("data", data);
        }

        return result;
    }

    private void saveDeliveryLog(String noticeId,
                                 String recipientId,
                                 String userId,
                                 UserDeviceEntity device,
                                 NoticeDeliveryLogStatus status,
                                 String providerMessageId,
                                 String errorCode,
                                 String errorMessage) {
        NoticeDeliveryLogEntity log = new NoticeDeliveryLogEntity();
        log.setNoticeId(noticeId);
        log.setRecipientId(recipientId);
        log.setUserId(userId);
        log.setDeviceId(device.getDeviceId());
        log.setFcmToken(device.getFcmToken());
        log.setProvider(NoticeProvider.FIREBASE);
        log.setProviderMessageId(providerMessageId);
        log.setStatus(status);
        log.setErrorCode(errorCode);
        log.setErrorMessage(errorMessage);
        deliveryLogRepository.save(log);
    }

    private NoticeDto toDto(NoticeRecipientEntity recipient) {
        NoticeDto dto = new NoticeDto();
        dto.setRecipientId(recipient.getId());
        dto.setNoticeId(recipient.getNoticeId());
        dto.setUserId(recipient.getUserId());
        dto.setDeliveryStatus(recipient.getDeliveryStatus());
        dto.setReadStatus(recipient.getReadStatus());
        dto.setSentAt(recipient.getSentAt());
        dto.setReadAt(recipient.getReadAt());

        noticeRepository.findEntityById(recipient.getNoticeId()).ifPresent(notice -> {
            dto.setTitle(notice.getTitle());
            dto.setContent(notice.getContent());
            dto.setNoticeType(notice.getNoticeType());
            dto.setTargetType(notice.getTargetType());
            dto.setData(notice.getData());
            dto.setStatus(notice.getStatus());
            if (dto.getSentAt() == null) {
                dto.setSentAt(notice.getSentAt());
            }
        });

        return dto;
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
}
