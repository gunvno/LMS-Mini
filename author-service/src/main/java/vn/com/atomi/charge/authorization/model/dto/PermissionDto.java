package vn.com.atomi.charge.authorization.model.dto;

import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;

@Getter
@Setter
public class PermissionDto extends BaseDto<String> {
    private String name;
    private String code;
    private String description;
    private String status;
}