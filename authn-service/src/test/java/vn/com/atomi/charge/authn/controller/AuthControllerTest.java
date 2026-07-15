package vn.com.atomi.charge.authn.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.com.atomi.charge.authn.model.dto.AuthenticationResult;
import vn.com.atomi.charge.authn.model.response.AuthenticationResponse;
import vn.com.atomi.charge.authn.service.interfaces.AuthnService;
import vn.com.atomi.charge.authn.service.internal.AuthCookieService;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private final AuthnService authnService = mock(AuthnService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthCookieService cookieService = new AuthCookieService();
        ReflectionTestUtils.setField(cookieService, "secure", true);
        ReflectionTestUtils.setField(cookieService, "sameSite", "Lax");
        ReflectionTestUtils.setField(cookieService, "domain", "");
        ReflectionTestUtils.setField(cookieService, "accessTokenDuration", 900L);
        ReflectionTestUtils.setField(cookieService, "refreshTokenDuration", 3600L);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authnService, cookieService))
                .build();
    }

    @Test
    void loginKeepsTokensOutOfJsonResponse() throws Exception {
        AuthenticationResponse response = AuthenticationResponse.builder()
                .id("user-1")
                .userName("student")
                .email("student@example.com")
                .build();
        when(authnService.authenticate(any())).thenReturn(
                new AuthenticationResult("access-secret", "refresh-secret", response));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"student\",\"password\":\"Valid@123\"}"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getHeaders("Set-Cookie"))
                        .anyMatch(value -> value.contains("lms_access_token="))
                        .anyMatch(value -> value.contains("lms_refresh_token=")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(content().string(not(containsString("access-secret"))))
                .andExpect(content().string(not(containsString("refresh-secret"))))
                .andExpect(content().string(not(containsString("\"token\""))))
                .andExpect(content().string(not(containsString("\"refreshToken\""))));
    }
}
