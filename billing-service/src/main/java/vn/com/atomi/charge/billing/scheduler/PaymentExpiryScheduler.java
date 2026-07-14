package vn.com.atomi.charge.billing.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.com.atomi.charge.billing.model.enums.PaymentStatus;
import vn.com.atomi.charge.billing.repository.PaymentRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpiryScheduler {

    private static final long PAYMENT_EXPIRY_HOURS = 24;
    private static final String EXPIRY_REASON = "expired-after-24-hours";

    private final PaymentRepository paymentRepository;

    @Scheduled(
            initialDelayString = "${payment.expiry.initial-delay-ms:10000}",
            fixedDelayString = "${payment.expiry.check-delay-ms:60000}"
    )
    @Transactional(rollbackFor = Exception.class)
    public void expirePendingPayments() {
        LocalDateTime now = LocalDateTime.now();
        int updated = paymentRepository.expirePendingPayments(
                PaymentStatus.PENDING,
                PaymentStatus.EXPIRED,
                now.minusHours(PAYMENT_EXPIRY_HOURS),
                now,
                EXPIRY_REASON
        );
        if (updated > 0) {
            log.info("Expired {} pending payment(s) older than {} hours", updated, PAYMENT_EXPIRY_HOURS);
        }
    }
}
