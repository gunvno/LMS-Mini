package vn.com.atomi.charge.notice.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.notice.model.enums.NoticeType;

import java.util.List;

@Getter
@Setter
public class NoticeSendUsersRequest {

    @NotEmpty
    private List<String> userIds;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private NoticeType noticeType;

    private String data;
}
