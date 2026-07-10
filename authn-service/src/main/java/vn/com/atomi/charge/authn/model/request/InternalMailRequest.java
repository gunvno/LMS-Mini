package vn.com.atomi.charge.authn.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalMailRequest {
    @NotBlank private String email;
    @NotBlank private String subject;
    @NotBlank private String content;
}
