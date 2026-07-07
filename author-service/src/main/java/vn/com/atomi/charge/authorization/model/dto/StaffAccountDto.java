package vn.com.atomi.charge.authorization.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class StaffAccountDto {
    private String userId;

    private String username;

    private String email;

    private String phone;

    private String fullName;

    private String roleCode;

    private List<String> permissionCodes = new ArrayList<>();
}
