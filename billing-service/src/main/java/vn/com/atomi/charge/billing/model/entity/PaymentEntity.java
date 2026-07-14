package vn.com.atomi.charge.billing.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.billing.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_payments")
public class PaymentEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "course_id", nullable = false)
    private String courseId;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "provider", nullable = false, length = 30)
    private String provider;

    @Column(name = "provider_order_code", unique = true)
    private Long providerOrderCode;

    @Column(name = "provider_payment_link_id", length = 150)
    private String providerPaymentLinkId;

    @Column(name = "provider_checkout_url", length = 500)
    private String providerCheckoutUrl;

    @Column(name = "provider_qr_code", columnDefinition = "TEXT")
    private String providerQrCode;

    @Column(name = "transfer_content", length = 100)
    private String transferContent;

    @Column(name = "provider_transaction_id", length = 150)
    private String providerTransactionId;

    @Column(name = "invoice_code", unique = true, length = 50)
    private String invoiceCode;

    @Column(name = "invoice_issued_at")
    private LocalDateTime invoiceIssuedAt;

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "raw_webhook", columnDefinition = "TEXT")
    private String rawWebhook;
}
