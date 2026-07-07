package vn.com.atomi.charge.authn.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationResponse {
    private String token;
    private String refreshToken;
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private String[] permissions;
}