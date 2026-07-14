package vn.com.atomi.charge.billing.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.billing.model.entity.PaymentEntity;
import vn.com.atomi.charge.billing.model.response.InvoiceResponse;

public interface InvoiceService {

    InvoiceResponse createOrGet(PaymentEntity payment);

    BaseResponse<Page<InvoiceResponse>> getMyInvoices(String userId, Pageable pageable);

    BaseResponse<Page<InvoiceResponse>> getAllInvoices(Pageable pageable);

    BaseResponse<InvoiceResponse> getMyInvoice(String userId, String invoiceCode);
}
