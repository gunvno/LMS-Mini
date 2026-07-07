package vn.com.atomi.charge.authn.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserInfoResponse {
    private String sub;
    private String email;
    private String preferred_username;
    private String given_name;
    private String family_name;
}