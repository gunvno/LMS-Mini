package vn.com.atomi.charge.base.model.constant;

import java.util.Arrays;
import java.util.List;

public class ConstantKey {

    public static final String TOKEN_AUTH_KEY = "Bearer";

    public static final String TOKEN_BEARER_PREFIX = "Bearer ";

    public static final String BASIC_AUTH_KEY = "Basic";

    public static final List<String> WHITELIST_API = Arrays.asList(
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-*/v3/api-docs",
            "/actuator/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/public/**",
            "/api/v1/public/**",
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/introspect",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/logout",
            "/api/v1/auth/otp-register",
            "/api/v1/auth/otp-verify",
            "/api/v1/courses/published",
            "/api/v1/courses/*/published",
            "/api/v1/courses/*/images/primary/view",
            "/internal/**"
    );

    public static final String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_])(?!.*\\s).{8,}$";

    public static final String passwordPatternSHA256 = "^[a-fA-F0-9]{64}$";

    public static final String addressPattern = "^(?! )[A-Za-zÀ-Ỵà-ỵ0-9\\-/ ]{1,200}(?<! )$";

    public static final String emailPattern = "^[A-Za-z0-9](?:[A-Za-z0-9._%+-]{0,62}[A-Za-z0-9])?@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z]{2,})+$";

    public static final String genderPattern = "(?i)^(nam|nữ|khác)$";

}
