package vn.com.atomi.charge.payment.model.dto;

import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.payment.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentDto extends BaseDto<String> {
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
    /** Creation timestamp for payment history and dashboard tables. */
    private LocalDateTime createdDate;
    /** Backward-compatible alias for clients that use camel-case `createdAt`. */
    private LocalDateTime createdAt;
    /** Always populated: paidAt for completed payments, otherwise createdDate. */
    private LocalDateTime displayDate;
}
