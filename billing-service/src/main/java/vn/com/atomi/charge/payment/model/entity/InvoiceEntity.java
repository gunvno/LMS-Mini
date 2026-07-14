package vn.com.atomi.charge.payment.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_invoices")
public class InvoiceEntity extends BaseEntity {

    @Column(name = "payment_id", nullable = false, unique = true, length = 36)
    private String paymentId;

    @Column(name = "invoice_code", nullable = false, unique = true, length = 50)
    private String invoiceCode;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "course_id", nullable = false, length = 36)
    private String courseId;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "provider", nullable = false, length = 30)
    private String provider;

    @Column(name = "provider_transaction_id", length = 150)
    private String providerTransactionId;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
