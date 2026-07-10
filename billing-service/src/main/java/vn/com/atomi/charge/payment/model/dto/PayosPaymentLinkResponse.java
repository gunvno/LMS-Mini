package vn.com.atomi.charge.payment.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayosPaymentLinkResponse {
    private String paymentLinkId;
    private Long orderCode;
    private Integer amount;
    private String description;
    private String checkoutUrl;
    private String qrCode;
    private String status;
}
