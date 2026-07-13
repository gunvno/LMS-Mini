package vn.com.atomi.charge.payment.model.response;

import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.payment.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentResponse extends BaseDto<String> {
    private String userId;
    private String courseId;
    private BigDecimal amount;
    private String provider;
    private Long providerOrderCode;
    private String providerPaymentLinkId;
    private String providerCheckoutUrl;
    private String providerQrCode;
    private String transferContent;
    private String providerTransactionId;
    private String invoiceCode;
    private LocalDateTime invoiceIssuedAt;
    private PaymentStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime createdDate;
    private LocalDateTime createdAt;
    private LocalDateTime displayDate;
}
