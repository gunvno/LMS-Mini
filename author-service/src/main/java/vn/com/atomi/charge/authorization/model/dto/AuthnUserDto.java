package vn.com.atomi.charge.authorization.model.dto;

import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;

import java.time.LocalDateTime;

@Getter
@Setter
public class AuthnUserDto extends BaseDto<String> {
    private String username;

    private String email;

    private String phone;

    private String fullName;

    private String status;

    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;

    private LocalDateTime lastLoginAt;
}
