package vn.com.atomi.charge.authorization.model.dto;

import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RoleDto extends BaseDto<String> {
    private String name;
    private String code;
    private String description;
    private String status;
    private List<String> permissionCodes = new ArrayList<>();
}