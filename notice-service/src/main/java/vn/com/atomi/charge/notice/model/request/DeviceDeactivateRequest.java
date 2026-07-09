package vn.com.atomi.charge.notice.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceDeactivateRequest {

    @NotBlank
    private String token;
}