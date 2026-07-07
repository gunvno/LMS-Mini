package vn.com.atomi.charge.authn.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpVerifyRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String inputOtp;

    @NotBlank
    private String expectedType;
}