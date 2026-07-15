package vn.com.atomi.charge.authorization.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NoticeRecipientOptionDto {
    private String userId;
    private String username;
    private String email;
    private String fullName;
    private String status;
    private List<String> roleCodes = new ArrayList<>();
}
