package vn.com.atomi.charge.base.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BaseErrorCode {
    INTERNAL_ERROR("EV-500"),
    BAD_REQUEST("EV-400"),
    UNAUTHORIZED("EV-401"),
    FORBIDDEN("EV-403"),
    SUCCESS("EV-200"),
    NOT_FOUND("EV-404"),
    METHOD_NOT_ALLOWED("EV-405"),
    FAILURE("EV-999"),
    COMMON_ERROR("common.internal_error"),
    ACCESS_DENIED("common.access_denied"),
    ;

    private final String errorCode;
}
