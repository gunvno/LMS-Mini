package vn.com.atomi.charge.notice.model.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Getter
@Setter
public class DeviceDeactivateRequest {

    private String installationId;

    /**
     * Backward-compatible alias used by clients that still name the Firebase
     * installation identifier "token".
     */
    @Deprecated
    private String token;

    public String resolveInstallationId() {
        return StringUtils.hasText(installationId) ? installationId.trim()
                : StringUtils.hasText(token) ? token.trim() : null;
    }
}
