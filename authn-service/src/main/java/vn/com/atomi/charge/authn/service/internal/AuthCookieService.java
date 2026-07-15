package vn.com.atomi.charge.authn.service.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.authn.model.dto.AuthenticationResult;
import vn.com.atomi.charge.authn.model.enums.ClientPortal;

import java.time.Duration;

@Service
public class AuthCookieService {

    private static final String LEGACY_ACCESS_TOKEN_COOKIE = "lms_access_token";
    private static final String LEGACY_REFRESH_TOKEN_COOKIE = "lms_refresh_token";

    @Value("${auth.cookie.secure:false}")
    private boolean secure;

    @Value("${auth.cookie.same-site:Lax}")
    private String sameSite;

    @Value("${auth.cookie.domain:}")
    private String domain;

    @Value("${jwt.valid-duration}")
    private long accessTokenDuration;

    @Value("${jwt.refreshable-duration}")
    private long refreshTokenDuration;

    public HttpHeaders sessionHeaders(AuthenticationResult result, ClientPortal portal) {
        HttpHeaders headers = noStoreHeaders();
        headers.add(HttpHeaders.SET_COOKIE,
                cookie(portal.accessTokenCookie(), result.accessToken(), accessTokenDuration).toString());
        headers.add(HttpHeaders.SET_COOKIE,
                cookie(portal.refreshTokenCookie(), result.refreshToken(), refreshTokenDuration).toString());
        return headers;
    }

    public HttpHeaders clearSessionHeaders(ClientPortal portal) {
        HttpHeaders headers = noStoreHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie(portal.accessTokenCookie(), "", 0).toString());
        headers.add(HttpHeaders.SET_COOKIE, cookie(portal.refreshTokenCookie(), "", 0).toString());
        headers.add(HttpHeaders.SET_COOKIE, cookie(LEGACY_ACCESS_TOKEN_COOKIE, "", 0).toString());
        headers.add(HttpHeaders.SET_COOKIE, cookie(LEGACY_REFRESH_TOKEN_COOKIE, "", 0).toString());
        return headers;
    }

    private ResponseCookie cookie(String name, String value, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds));
        if (StringUtils.hasText(domain)) {
            builder.domain(domain.trim());
        }
        return builder.build();
    }

    private HttpHeaders noStoreHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-store");
        headers.setPragma("no-cache");
        return headers;
    }
}
