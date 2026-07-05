package vn.com.atomi.charge.base.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomHeader {

    USER_INFO("X-User-Info"),
    ROLE_CODE("X-Role-Code"),
    PHONE_NUMBER("X-User"),
    ;

    private final String headerName;
}
