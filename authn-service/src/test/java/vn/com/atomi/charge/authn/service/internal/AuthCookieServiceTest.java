package vn.com.atomi.charge.authn.service.internal;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import vn.com.atomi.charge.authn.model.dto.AuthenticationResult;
import vn.com.atomi.charge.authn.model.enums.ClientPortal;
import vn.com.atomi.charge.authn.model.response.AuthenticationResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthCookieServiceTest {

    @Test
    void sessionTokensAreOnlyWrittenToHttpOnlyCookies() {
        AuthCookieService service = cookieService();
        AuthenticationResponse publicResponse = AuthenticationResponse.builder()
                .id("user-1")
                .userName("student")
                .build();

        HttpHeaders headers = service.sessionHeaders(
                new AuthenticationResult("access-secret", "refresh-secret", publicResponse),
                ClientPortal.STUDENT);
        List<String> cookies = headers.get(HttpHeaders.SET_COOKIE);

        assertThat(cookies).hasSize(2);
        assertThat(cookies).anyMatch(cookie -> cookie.startsWith("lms_student_access_token="));
        assertThat(cookies).anyMatch(cookie -> cookie.startsWith("lms_student_refresh_token="));
        assertThat(cookies).noneMatch(cookie -> cookie.startsWith("lms_admin_"));
        assertThat(cookies).allMatch(cookie -> cookie.contains("HttpOnly"));
        assertThat(cookies).allMatch(cookie -> cookie.contains("Secure"));
        assertThat(cookies).allMatch(cookie -> cookie.contains("SameSite=Lax"));
        assertThat(headers.getCacheControl()).isEqualTo("no-store");
        assertThat(publicResponse).hasNoNullFieldsOrPropertiesExcept(
                "email", "firstName", "lastName");
    }

    @Test
    void adminAndStudentSessionsUseDifferentCookieNames() {
        AuthCookieService service = cookieService();
        AuthenticationResult result = new AuthenticationResult(
                "access-secret",
                "refresh-secret",
                AuthenticationResponse.builder().id("user-1").userName("user").build());

        List<String> studentCookies = service.sessionHeaders(result, ClientPortal.STUDENT)
                .get(HttpHeaders.SET_COOKIE);
        List<String> adminCookies = service.sessionHeaders(result, ClientPortal.ADMIN)
                .get(HttpHeaders.SET_COOKIE);

        assertThat(studentCookies).extracting(AuthCookieServiceTest::cookieName)
                .containsExactly("lms_student_access_token", "lms_student_refresh_token");
        assertThat(adminCookies).extracting(AuthCookieServiceTest::cookieName)
                .containsExactly("lms_admin_access_token", "lms_admin_refresh_token");
    }

    private static String cookieName(String cookie) {
        return cookie.substring(0, cookie.indexOf('='));
    }

    private static AuthCookieService cookieService() {
        AuthCookieService service = new AuthCookieService();
        ReflectionTestUtils.setField(service, "secure", true);
        ReflectionTestUtils.setField(service, "sameSite", "Lax");
        ReflectionTestUtils.setField(service, "domain", "");
        ReflectionTestUtils.setField(service, "accessTokenDuration", 900L);
        ReflectionTestUtils.setField(service, "refreshTokenDuration", 3600L);
        return service;
    }
}
