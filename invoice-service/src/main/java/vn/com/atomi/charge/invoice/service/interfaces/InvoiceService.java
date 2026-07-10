package vn.com.atomi.charge.invoice.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.invoice.model.dto.InvoiceDto;

public interface InvoiceService {
    BaseResponse<InvoiceDto> createOrGet(InvoiceDto request);
    BaseResponse<Page<InvoiceDto>> getMyInvoices(String userId, Pageable pageable);
    BaseResponse<Page<InvoiceDto>> getAllInvoices(Pageable pageable);
    BaseResponse<InvoiceDto> getMyInvoice(String userId, String invoiceCode);
}
