package vn.com.atomi.charge.authorization.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RolePermissionRequest {
    @Valid
    @NotNull
    private List<String> permissionCodes = new ArrayList<>();
}