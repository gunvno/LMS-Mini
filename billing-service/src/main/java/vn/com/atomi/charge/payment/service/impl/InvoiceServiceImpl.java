package vn.com.atomi.charge.payment.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.payment.mapper.InvoiceMapper;
import vn.com.atomi.charge.payment.model.entity.InvoiceEntity;
import vn.com.atomi.charge.payment.model.entity.PaymentEntity;
import vn.com.atomi.charge.payment.model.response.InvoiceResponse;
import vn.com.atomi.charge.payment.repository.InvoiceRepository;
import vn.com.atomi.charge.payment.service.interfaces.InvoiceService;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public InvoiceResponse createOrGet(PaymentEntity payment) {
        if (payment == null
                || !StringUtils.hasText(payment.getId())
                || !StringUtils.hasText(payment.getInvoiceCode())
                || payment.getInvoiceIssuedAt() == null) {
            throw new IllegalArgumentException("invoice.invalid_request");
        }
        InvoiceEntity invoice = invoiceRepository.findFirstByPaymentIdAndDeletedAtIsNull(payment.getId())
                .orElseGet(() -> invoiceRepository.save(invoiceMapper.toEntity(payment)));
        return invoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<Page<InvoiceResponse>> getMyInvoices(String userId, Pageable pageable) {
        if (!StringUtils.hasText(userId)) {
            return BaseResponse.fail(HttpStatus.UNAUTHORIZED, "user.not_found");
        }
        Page<InvoiceResponse> invoices = invoiceRepository.findByUserIdAndDeletedAtIsNull(userId, pageable)
                .map(invoiceMapper::toResponse);
        return BaseResponse.success(HttpStatus.OK, invoices);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<Page<InvoiceResponse>> getAllInvoices(Pageable pageable) {
        return BaseResponse.success(HttpStatus.OK, invoiceRepository.getAll(pageable).map(invoiceMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<InvoiceResponse> getMyInvoice(String userId, String invoiceCode) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(invoiceCode)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "invoice.not_found");
        }
        return invoiceRepository.findFirstByUserIdAndInvoiceCodeAndDeletedAtIsNull(userId, invoiceCode)
                .map(invoice -> BaseResponse.success(HttpStatus.OK, invoiceMapper.toResponse(invoice)))
                .orElseGet(() -> BaseResponse.fail(HttpStatus.BAD_REQUEST, "invoice.not_found"));
    }
}
