package vn.com.atomi.charge.payment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.payment.model.entity.PaymentEntity;
import vn.com.atomi.charge.payment.model.enums.PaymentStatus;

import java.time.LocalDateTime;
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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PaymentEntity payment
               set payment.status = :expiredStatus,
                   payment.rawWebhook = :reason,
                   payment.lastModifiedDate = :now,
                   payment.version = payment.version + 1
             where payment.status = :pendingStatus
               and payment.createdDate <= :expiredBefore
               and payment.deletedAt is null
            """)
    int expirePendingPayments(
            @Param("pendingStatus") PaymentStatus pendingStatus,
            @Param("expiredStatus") PaymentStatus expiredStatus,
            @Param("expiredBefore") LocalDateTime expiredBefore,
            @Param("now") LocalDateTime now,
            @Param("reason") String reason
    );
}
