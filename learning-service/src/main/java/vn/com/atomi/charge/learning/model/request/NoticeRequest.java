package vn.com.atomi.charge.learning.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeRequest {
    private String userId;
    private String roleCode;
    private String title;
    private String content;
    private String noticeType;
    private String data;
}
