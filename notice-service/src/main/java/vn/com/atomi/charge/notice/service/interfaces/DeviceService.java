package vn.com.atomi.charge.notice.service.interfaces;

import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.notice.model.entity.UserDeviceEntity;
import vn.com.atomi.charge.notice.model.request.DeviceDeactivateRequest;
import vn.com.atomi.charge.notice.model.request.DeviceRegisterRequest;

import java.util.List;

public interface DeviceService {

    BaseResponse<Void> registerDevice(BaseRequest<DeviceRegisterRequest> request);

    BaseResponse<Void> deactivateDevice(BaseRequest<DeviceDeactivateRequest> request);

    List<UserDeviceEntity> getActiveDevices(String userId);

    List<UserDeviceEntity> getAllActiveDevices();

    void markInstallationInvalid(String installationId);
}
