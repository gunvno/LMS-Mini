package vn.com.atomi.charge.invoice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.invoice.model.dto.InvoiceDto;
import vn.com.atomi.charge.invoice.model.entity.InvoiceEntity;
import vn.com.atomi.charge.invoice.repository.InvoiceRepository;
import vn.com.atomi.charge.invoice.service.interfaces.InvoiceService;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional
    public BaseResponse<InvoiceDto> createOrGet(InvoiceDto request) {
        if (request == null || !StringUtils.hasText(request.getPaymentId()) || !StringUtils.hasText(request.getInvoiceCode())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "invoice.invalid_request");
        }
        InvoiceEntity invoice = invoiceRepository.findFirstByPaymentIdAndDeletedAtIsNull(request.getPaymentId())
                .orElseGet(() -> invoiceRepository.save(toEntity(request)));
        return BaseResponse.success(HttpStatus.OK, toDto(invoice));
    }

    @Override
    public BaseResponse<Page<InvoiceDto>> getMyInvoices(String userId, Pageable pageable) {
        if (!StringUtils.hasText(userId)) return BaseResponse.fail(HttpStatus.UNAUTHORIZED, "user.not_found");
        return BaseResponse.success(HttpStatus.OK, invoiceRepository.findByUserIdAndDeletedAtIsNull(userId, pageable).map(this::toDto));
    }

    @Override
    public BaseResponse<Page<InvoiceDto>> getAllInvoices(Pageable pageable) {
        return BaseResponse.success(HttpStatus.OK, invoiceRepository.getAll(pageable).map(this::toDto));
    }

    @Override
    public BaseResponse<InvoiceDto> getMyInvoice(String userId, String invoiceCode) {
        return invoiceRepository.findFirstByUserIdAndInvoiceCodeAndDeletedAtIsNull(userId, invoiceCode)
                .map(invoice -> BaseResponse.success(HttpStatus.OK, toDto(invoice)))
                .orElseGet(() -> BaseResponse.fail(HttpStatus.BAD_REQUEST, "invoice.not_found"));
    }

    private InvoiceEntity toEntity(InvoiceDto dto) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.setPaymentId(dto.getPaymentId());
        entity.setInvoiceCode(dto.getInvoiceCode());
        entity.setUserId(dto.getUserId());
        entity.setCourseId(dto.getCourseId());
        entity.setAmount(dto.getAmount());
        entity.setProvider(dto.getProvider());
        entity.setProviderTransactionId(dto.getProviderTransactionId());
        entity.setStatus(dto.getStatus());
        entity.setIssuedAt(dto.getIssuedAt());
        entity.setPaidAt(dto.getPaidAt());
        return entity;
    }

    private InvoiceDto toDto(InvoiceEntity entity) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(entity.getId());
        dto.setPaymentId(entity.getPaymentId());
        dto.setInvoiceCode(entity.getInvoiceCode());
        dto.setUserId(entity.getUserId());
        dto.setCourseId(entity.getCourseId());
        dto.setAmount(entity.getAmount());
        dto.setProvider(entity.getProvider());
        dto.setProviderTransactionId(entity.getProviderTransactionId());
        dto.setStatus(entity.getStatus());
        dto.setIssuedAt(entity.getIssuedAt());
        dto.setPaidAt(entity.getPaidAt());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setCreatedAt(entity.getCreatedDate());
        dto.setDisplayDate(entity.getPaidAt() == null ? entity.getCreatedDate() : entity.getPaidAt());
        return dto;
    }
}
