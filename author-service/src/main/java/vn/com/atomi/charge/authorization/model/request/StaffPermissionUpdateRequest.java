package vn.com.atomi.charge.authorization.model.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class StaffPermissionUpdateRequest {
    private List<String> permissionCodes = new ArrayList<>();
}
