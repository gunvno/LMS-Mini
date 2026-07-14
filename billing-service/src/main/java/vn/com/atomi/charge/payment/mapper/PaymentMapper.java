package vn.com.atomi.charge.payment.mapper;

import org.springframework.stereotype.Component;
import vn.com.atomi.charge.payment.model.entity.PaymentEntity;
import vn.com.atomi.charge.payment.model.response.PaymentResponse;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(PaymentEntity entity) {
        PaymentResponse response = new PaymentResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setCourseId(entity.getCourseId());
        response.setAmount(entity.getAmount());
        response.setProvider(entity.getProvider());
        response.setProviderOrderCode(entity.getProviderOrderCode());
        response.setProviderPaymentLinkId(entity.getProviderPaymentLinkId());
        response.setProviderCheckoutUrl(entity.getProviderCheckoutUrl());
        response.setProviderQrCode(entity.getProviderQrCode());
        response.setTransferContent(entity.getTransferContent());
        response.setProviderTransactionId(entity.getProviderTransactionId());
        response.setInvoiceCode(entity.getInvoiceCode());
        response.setInvoiceIssuedAt(entity.getInvoiceIssuedAt());
        response.setStatus(entity.getStatus());
        response.setPaidAt(entity.getPaidAt());
        response.setCreatedDate(entity.getCreatedDate());
        response.setCreatedAt(entity.getCreatedDate());
        response.setDisplayDate(entity.getPaidAt() == null
                ? entity.getCreatedDate()
                : entity.getPaidAt());
        return response;
    }
}
