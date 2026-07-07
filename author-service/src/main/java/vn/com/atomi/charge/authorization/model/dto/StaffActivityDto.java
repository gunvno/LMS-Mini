package vn.com.atomi.charge.authorization.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StaffActivityDto {
    private String userId;

    private String username;

    private String fullName;

    private String status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;
}
