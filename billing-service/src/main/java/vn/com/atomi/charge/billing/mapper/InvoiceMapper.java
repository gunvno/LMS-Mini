package vn.com.atomi.charge.billing.mapper;

import org.springframework.stereotype.Component;
import vn.com.atomi.charge.billing.model.entity.InvoiceEntity;
import vn.com.atomi.charge.billing.model.entity.PaymentEntity;
import vn.com.atomi.charge.billing.model.response.InvoiceResponse;

@Component
public class InvoiceMapper {

    public InvoiceEntity toEntity(PaymentEntity payment) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.setPaymentId(payment.getId());
        entity.setInvoiceCode(payment.getInvoiceCode());
        entity.setUserId(payment.getUserId());
        entity.setCourseId(payment.getCourseId());
        entity.setAmount(payment.getAmount());
        entity.setProvider(payment.getProvider());
        entity.setProviderTransactionId(payment.getProviderTransactionId());
        entity.setStatus(payment.getStatus().name());
        entity.setIssuedAt(payment.getInvoiceIssuedAt());
        entity.setPaidAt(payment.getPaidAt());
        return entity;
    }

    public InvoiceResponse toResponse(InvoiceEntity entity) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(entity.getId());
        response.setPaymentId(entity.getPaymentId());
        response.setInvoiceCode(entity.getInvoiceCode());
        response.setUserId(entity.getUserId());
        response.setCourseId(entity.getCourseId());
        response.setAmount(entity.getAmount());
        response.setProvider(entity.getProvider());
        response.setProviderTransactionId(entity.getProviderTransactionId());
        response.setStatus(entity.getStatus());
        response.setIssuedAt(entity.getIssuedAt());
        response.setPaidAt(entity.getPaidAt());
        response.setCreatedDate(entity.getCreatedDate());
        response.setCreatedAt(entity.getCreatedDate());
        response.setDisplayDate(entity.getPaidAt() == null ? entity.getCreatedDate() : entity.getPaidAt());
        return response;
    }
}
