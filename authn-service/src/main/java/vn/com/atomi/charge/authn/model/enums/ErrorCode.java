package vn.com.atomi.charge.authn.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_PASSWORD("AUTHN_999"),
    LOGIN_METHOD_INVALID("AUTHN_998"),
    LOGIN_OTHER_DEVICE_BY_KYC("AUTHN_997"),
    INVALID_USERNAME("AUTHN_996"),
    LOCKED_USER("AUTHN_995"),
    LOGIN_FAILED("AUTHN_994"),
    UNLOCK_TIME("AUTHN_993"),
    INVALID_CONFIRM_PASSWORD("AUTHN_992"),
    INVALID_NEW_PASSWORD("AUTHN_991"),
    INVALID_OLD_PASSWORD("AUTHN_990"),
    OLD_PASSWORD_EQUAL_NEW_PASSWORD("AUTHN_989"),
    INVALID_OLD_PASSWORD_2("AUTHN_988"),
    INVALID_PASSWORD_KYC_LOGIN("AUTHN_987"),
    EXCEED_OTP_LIMIT("forgot-password.send-otp-limit"),
    USER_NOT_FOUND("user.user_not_found"),
    ;
    private final String errorCode;
}
