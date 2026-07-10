package vn.com.atomi.charge.authn.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordOtpRequest {
    @Email
    @NotBlank
    private String email;
}
