package vn.com.atomi.charge.notice.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.notice.model.enums.NoticeType;

@Getter
@Setter
public class NoticeSendRoleRequest {

    @NotBlank
    private String roleCode;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private NoticeType noticeType;

    private String data;
}
