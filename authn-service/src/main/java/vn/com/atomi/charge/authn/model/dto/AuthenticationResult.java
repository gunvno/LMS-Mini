package vn.com.atomi.charge.authn.model.dto;

import vn.com.atomi.charge.authn.model.response.AuthenticationResponse;

public record AuthenticationResult(
        String accessToken,
        String refreshToken,
        AuthenticationResponse response
) {
}
