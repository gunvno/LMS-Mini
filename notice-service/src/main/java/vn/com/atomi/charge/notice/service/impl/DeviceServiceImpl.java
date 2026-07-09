package vn.com.atomi.charge.notice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.notice.model.entity.UserDeviceEntity;
import vn.com.atomi.charge.notice.model.enums.UserDeviceStatus;
import vn.com.atomi.charge.notice.model.request.DeviceDeactivateRequest;
import vn.com.atomi.charge.notice.model.request.DeviceRegisterRequest;
import vn.com.atomi.charge.notice.repository.UserDeviceRepository;
import vn.com.atomi.charge.notice.service.interfaces.DeviceService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final UserDeviceRepository repository;

    @Override
    public BaseResponse<Void> registerDevice(BaseRequest<DeviceRegisterRequest> request) {
        String userId = currentUserId();
        if (!StringUtils.hasText(userId)) {
            return BaseResponse.fail(HttpStatus.UNAUTHORIZED, "user.not_found");
        }

        DeviceRegisterRequest data = request == null ? null : request.getData();
        if (data == null || !StringUtils.hasText(data.getToken())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "device.token_required");
        }

        UserDeviceEntity entity = repository
                .findByUserIdAndFcmTokenAndDeletedAtIsNull(userId, data.getToken())
                .orElseGet(UserDeviceEntity::new);

        entity.setUserId(userId);
        entity.setFcmToken(data.getToken());
        entity.setDeviceType(data.getDeviceType());
        entity.setDeviceId(data.getDeviceId());
        entity.setAppVersion(data.getAppVersion());
        entity.setStatus(UserDeviceStatus.ACTIVE);
        entity.setLastActiveAt(LocalDateTime.now());

        repository.save(entity);

        return BaseResponse.success(HttpStatus.OK, null);
    }

    @Override
    public BaseResponse<Void> deactivateDevice(BaseRequest<DeviceDeactivateRequest> request) {
        String userId = currentUserId();
        if (!StringUtils.hasText(userId)) {
            return BaseResponse.fail(HttpStatus.UNAUTHORIZED, "user.not_found");
        }

        DeviceDeactivateRequest data = request == null ? null : request.getData();
        if (data == null || !StringUtils.hasText(data.getToken())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "device.token_required");
        }

        repository.findByUserIdAndFcmTokenAndDeletedAtIsNull(userId, data.getToken())
                .ifPresent(device -> {
                    device.setStatus(UserDeviceStatus.INACTIVE);
                    device.setLastActiveAt(LocalDateTime.now());
                    repository.save(device);
                });

        return BaseResponse.success(HttpStatus.OK, null);
    }

    @Override
    public List<UserDeviceEntity> getActiveDevices(String userId) {
        if (!StringUtils.hasText(userId)) {
            return List.of();
        }

        return repository.findByUserIdAndStatusAndDeletedAtIsNull(userId, UserDeviceStatus.ACTIVE);
    }

    @Override
    public List<UserDeviceEntity> getAllActiveDevices() {
        return repository.findByStatusAndDeletedAtIsNull(UserDeviceStatus.ACTIVE);
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
}
