package vn.com.atomi.charge.authn.model.enums;

import java.util.Locale;

public enum ClientPortal {
    STUDENT,
    ADMIN;

    public static ClientPortal from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public String accessTokenCookie() {
        return "lms_" + name().toLowerCase(Locale.ROOT) + "_access_token";
    }

    public String refreshTokenCookie() {
        return "lms_" + name().toLowerCase(Locale.ROOT) + "_refresh_token";
    }
}
