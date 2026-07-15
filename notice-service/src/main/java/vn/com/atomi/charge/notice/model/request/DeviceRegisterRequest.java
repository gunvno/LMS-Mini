package vn.com.atomi.charge.notice.model.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.notice.model.enums.DeviceType;

@Getter
@Setter
public class DeviceRegisterRequest {

    private String installationId;

    /**
     * Backward-compatible alias used by clients that still name the Firebase
     * installation identifier "token".
     */
    @Deprecated
    private String token;

    private DeviceType deviceType;

    private String deviceId;

    private String appVersion;

    public String resolveInstallationId() {
        return StringUtils.hasText(installationId) ? installationId.trim()
                : StringUtils.hasText(token) ? token.trim() : null;
    }
}
