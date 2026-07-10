package vn.com.atomi.charge.payment.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class PayosWebhookRequest {
    private String code;
    private String desc;
    private Boolean success;
    private Map<String, Object> data;
    private String signature;
}
