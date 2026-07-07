package vn.com.atomi.charge.gateway.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthnUserInfoDto {
    private String sub;

    private String email;

    private String preferred_username;

    private String given_name;

    private String family_name;
}
