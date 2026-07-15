package vn.com.atomi.charge.notice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public BaseResponse<Void> registerDevice(BaseRequest<DeviceRegisterRequest> request) {
        String userId = currentUserId();
        if (!StringUtils.hasText(userId)) {
            return BaseResponse.fail(HttpStatus.UNAUTHORIZED, "user.not_found");
        }

        DeviceRegisterRequest data = request == null ? null : request.getData();
        String installationId = data == null ? null : data.resolveInstallationId();
        if (!StringUtils.hasText(installationId)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "device.installation_id_required");
        }

        List<UserDeviceEntity> registrations = repository.findAllByInstallationIdForUpdate(installationId);
        UserDeviceEntity owner = registrations.stream()
                .filter(device -> userId.equals(device.getUserId()))
                .findFirst()
                .orElseGet(() -> registrations.stream().findFirst().orElseGet(UserDeviceEntity::new));
        LocalDateTime now = LocalDateTime.now();

        registrations.stream()
                .filter(device -> device != owner)
                .forEach(device -> {
                    device.setStatus(UserDeviceStatus.INACTIVE);
                    device.setLastActiveAt(now);
                });

        owner.setUserId(userId);
        owner.setInstallationId(installationId);
        owner.setDeviceType(data.getDeviceType());
        owner.setDeviceId(data.getDeviceId());
        owner.setAppVersion(data.getAppVersion());
        owner.setStatus(UserDeviceStatus.ACTIVE);
        owner.setLastActiveAt(now);

        if (registrations.isEmpty()) {
            repository.save(owner);
        } else {
            repository.saveAll(registrations);
        }

        return BaseResponse.success(HttpStatus.OK, null);
    }

    @Override
    @Transactional
    public BaseResponse<Void> deactivateDevice(BaseRequest<DeviceDeactivateRequest> request) {
        String userId = currentUserId();
        if (!StringUtils.hasText(userId)) {
            return BaseResponse.fail(HttpStatus.UNAUTHORIZED, "user.not_found");
        }

        DeviceDeactivateRequest data = request == null ? null : request.getData();
        String installationId = data == null ? null : data.resolveInstallationId();
        if (!StringUtils.hasText(installationId)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "device.installation_id_required");
        }

        LocalDateTime now = LocalDateTime.now();
        List<UserDeviceEntity> ownedRegistrations = repository
                .findAllByInstallationIdForUpdate(installationId)
                .stream()
                .filter(device -> userId.equals(device.getUserId()))
                .toList();

        ownedRegistrations.forEach(device -> {
                    device.setStatus(UserDeviceStatus.INACTIVE);
                    device.setLastActiveAt(now);
                });
        repository.saveAll(ownedRegistrations);

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

    @Override
    @Transactional
    public void markInstallationInvalid(String installationId) {
        if (!StringUtils.hasText(installationId)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<UserDeviceEntity> registrations = repository.findAllByInstallationIdForUpdate(installationId);
        registrations.forEach(device -> {
            device.setStatus(UserDeviceStatus.INVALID);
            device.setLastActiveAt(now);
        });
        repository.saveAll(registrations);
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
}
