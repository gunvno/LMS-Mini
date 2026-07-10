package vn.com.atomi.charge.learning.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalMailRequest {
    private String email;
    private String subject;
    private String content;
}
