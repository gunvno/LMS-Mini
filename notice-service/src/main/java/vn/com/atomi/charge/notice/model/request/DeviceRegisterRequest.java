package vn.com.atomi.charge.notice.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.notice.model.enums.DeviceType;

@Getter
@Setter
public class DeviceRegisterRequest {

    @NotBlank
    private String token;

    private DeviceType deviceType;

    private String deviceId;

    private String appVersion;
}
