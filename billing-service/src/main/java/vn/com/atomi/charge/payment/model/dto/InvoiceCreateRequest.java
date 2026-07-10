package vn.com.atomi.charge.payment.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class InvoiceCreateRequest {
    private String paymentId;
    private String invoiceCode;
    private String userId;
    private String courseId;
    private BigDecimal amount;
    private String provider;
    private String providerTransactionId;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime paidAt;
}
