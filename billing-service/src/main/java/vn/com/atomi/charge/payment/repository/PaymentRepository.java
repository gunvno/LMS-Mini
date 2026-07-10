package vn.com.atomi.charge.payment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.payment.model.entity.PaymentEntity;
import vn.com.atomi.charge.payment.model.enums.PaymentStatus;

import java.util.Optional;

public interface PaymentRepository extends BaseRepository<PaymentEntity, String> {
    Optional<PaymentEntity> findFirstByProviderOrderCodeAndDeletedAtIsNull(Long providerOrderCode);
    Optional<PaymentEntity> findFirstByUserIdAndProviderOrderCodeAndDeletedAtIsNull(String userId, Long providerOrderCode);
    Optional<PaymentEntity> findFirstByUserIdAndInvoiceCodeAndDeletedAtIsNull(String userId, String invoiceCode);
    Optional<PaymentEntity> findFirstByUserIdAndCourseIdAndStatusAndDeletedAtIsNullOrderByCreatedDateDesc(
            String userId,
            String courseId,
            PaymentStatus status
    );
    Page<PaymentEntity> findByUserIdAndDeletedAtIsNull(String userId, Pageable pageable);
}
