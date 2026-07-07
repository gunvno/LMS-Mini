package vn.com.atomi.charge.authorization.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffStatusUpdateRequest {
    @NotBlank
    private String status;
}
