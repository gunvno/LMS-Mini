package vn.com.atomi.charge.billing.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.billing.model.entity.InvoiceEntity;

import java.util.Optional;

public interface InvoiceRepository extends BaseRepository<InvoiceEntity, String> {

    Optional<InvoiceEntity> findFirstByPaymentIdAndDeletedAtIsNull(String paymentId);

    Optional<InvoiceEntity> findFirstByUserIdAndInvoiceCodeAndDeletedAtIsNull(String userId, String invoiceCode);

    Page<InvoiceEntity> findByUserIdAndDeletedAtIsNull(String userId, Pageable pageable);
}
