package vn.com.atomi.charge.authn.service.internal;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import vn.com.atomi.charge.authn.model.dto.AuthenticationResult;
import vn.com.atomi.charge.authn.model.response.AuthenticationResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthCookieServiceTest {

    @Test
    void sessionTokensAreOnlyWrittenToHttpOnlyCookies() {
        AuthCookieService service = new AuthCookieService();
        ReflectionTestUtils.setField(service, "secure", true);
        ReflectionTestUtils.setField(service, "sameSite", "Lax");
        ReflectionTestUtils.setField(service, "domain", "");
        ReflectionTestUtils.setField(service, "accessTokenDuration", 900L);
        ReflectionTestUtils.setField(service, "refreshTokenDuration", 3600L);
        AuthenticationResponse publicResponse = AuthenticationResponse.builder()
                .id("user-1")
                .userName("student")
                .build();

        HttpHeaders headers = service.sessionHeaders(
                new AuthenticationResult("access-secret", "refresh-secret", publicResponse));
        List<String> cookies = headers.get(HttpHeaders.SET_COOKIE);

        assertThat(cookies).hasSize(2);
        assertThat(cookies).allMatch(cookie -> cookie.contains("HttpOnly"));
        assertThat(cookies).allMatch(cookie -> cookie.contains("Secure"));
        assertThat(cookies).allMatch(cookie -> cookie.contains("SameSite=Lax"));
        assertThat(headers.getCacheControl()).isEqualTo("no-store");
        assertThat(publicResponse).hasNoNullFieldsOrPropertiesExcept(
                "email", "firstName", "lastName");
    }
}
