package vn.com.atomi.charge.payment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.payment.client.CourseClient;
import vn.com.atomi.charge.payment.client.InvoiceClient;
import vn.com.atomi.charge.payment.client.LearningClient;
import vn.com.atomi.charge.payment.client.PayosClient;
import vn.com.atomi.charge.payment.model.dto.CourseDto;
import vn.com.atomi.charge.payment.model.entity.PaymentEntity;
import vn.com.atomi.charge.payment.model.enums.PaymentStatus;
import vn.com.atomi.charge.payment.model.request.CreateCoursePaymentRequest;
import vn.com.atomi.charge.payment.model.request.InvoiceCreateRequest;
import vn.com.atomi.charge.payment.model.request.PayosWebhookRequest;
import vn.com.atomi.charge.payment.model.response.PaymentResponse;
import vn.com.atomi.charge.payment.model.response.PayosPaymentLinkResponse;
import vn.com.atomi.charge.payment.repository.PaymentRepository;
import vn.com.atomi.charge.payment.service.interfaces.PaymentService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final long PAYMENT_EXPIRY_HOURS = 24;
    private static final String PROVIDER_PAYOS = "PAYOS";
    private static final String PROVIDER_FREE = "FREE";
    private static final DateTimeFormatter INVOICE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PaymentRepository paymentRepository;
    private final CourseClient courseClient;
    private final InvoiceClient invoiceClient;
    private final LearningClient learningClient;
    private final PayosClient payosClient;
    private final ObjectMapper objectMapper;

    @Value("${internal.service-key}")
    private String internalServiceKey;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<PaymentResponse> createCoursePayment(BaseRequest<CreateCoursePaymentRequest> request) {
        String userId = currentUserId();
        if (!StringUtils.hasText(userId)) {
            return BaseResponse.fail(HttpStatus.UNAUTHORIZED, "user.not_found");
        }
        String courseId = request == null || request.getData() == null ? null : request.getData().getCourseId();
        if (!StringUtils.hasText(courseId)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "course.not_found");
        }

        BaseResponse<CourseDto> courseResponse = courseClient.getPublishedCourse(courseId);
        CourseDto course = courseResponse == null ? null : courseResponse.getData();
        if (course == null || course.getPrice() == null) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "course.not_found");
        }

        BigDecimal amount = course.getPrice();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            PaymentEntity payment = createFreePayment(userId, courseId);
            if (!provisionPaidCourse(payment)) {
                return BaseResponse.fail(HttpStatus.SERVICE_UNAVAILABLE, "payment.enrollment_pending");
            }
            return BaseResponse.success(HttpStatus.OK, toResponse(payment));
        }

        Optional<PaymentEntity> paid = paymentRepository
                .findFirstByUserIdAndCourseIdAndStatusAndDeletedAtIsNullOrderByCreatedDateDesc(
                        userId, courseId, PaymentStatus.PAID);
        if (paid.isPresent()) {
            if (!provisionPaidCourse(paid.get())) {
                return BaseResponse.fail(HttpStatus.SERVICE_UNAVAILABLE, "payment.enrollment_pending");
            }
            return BaseResponse.success(HttpStatus.OK, toResponse(paid.get()));
        }

        Optional<PaymentEntity> pending = paymentRepository
                .findFirstByUserIdAndCourseIdAndStatusAndDeletedAtIsNullOrderByCreatedDateDesc(
                        userId, courseId, PaymentStatus.PENDING);
        if (pending.isPresent()) {
            PaymentEntity existingPending = expireIfNeeded(pending.get());
            if (existingPending.getStatus() == PaymentStatus.PENDING
                    && StringUtils.hasText(existingPending.getProviderCheckoutUrl())) {
                return BaseResponse.success(HttpStatus.OK, toResponse(existingPending));
            }
        }
        if (!payosClient.configured()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "payos.not_configured");
        }
        Integer payosAmount = toPayosAmount(amount);
        if (payosAmount == null) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "payment.amount_invalid");
        }

        Long orderCode = System.currentTimeMillis();
        String transferContent = "LMS" + orderCode;

        PaymentEntity payment = new PaymentEntity();
        payment.setUserId(userId);
        payment.setCourseId(courseId);
        payment.setAmount(amount);
        payment.setProvider(PROVIDER_PAYOS);
        payment.setProviderOrderCode(orderCode);
        payment.setInvoiceCode(invoiceCode(orderCode));
        payment.setTransferContent(transferContent);
        payment.setStatus(PaymentStatus.PENDING);

        PayosPaymentLinkResponse link = payosClient.createPaymentLink(orderCode, payosAmount, transferContent);
        payment.setProviderPaymentLinkId(link.getPaymentLinkId());
        payment.setProviderCheckoutUrl(link.getCheckoutUrl());
        payment.setProviderQrCode(link.getQrCode());

        return BaseResponse.success(HttpStatus.OK, toResponse(paymentRepository.save(payment)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Page<PaymentResponse>> getMyPayments(Pageable pageable) {
        String userId = currentUserId();
        if (!StringUtils.hasText(userId)) {
            return BaseResponse.fail(HttpStatus.UNAUTHORIZED, "user.not_found");
        }
        expirePendingPayments();
        return BaseResponse.success(HttpStatus.OK,
                paymentRepository.findByUserIdAndDeletedAtIsNull(userId, pageable).map(this::toResponse));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Page<PaymentResponse>> getAllPayments(Pageable pageable) {
        expirePendingPayments();
        return BaseResponse.success(HttpStatus.OK,
                paymentRepository.getAll(pageable).map(this::toResponse));
    }

    @Override
    public BaseResponse<PaymentResponse> getPayment(String id) {
        String userId = currentUserId();
        Optional<PaymentEntity> payment = paymentRepository.findEntityById(id)
                .filter(item -> item.getUserId().equals(userId));
        return payment
                .map(entity -> BaseResponse.success(HttpStatus.OK, toResponse(entity)))
                .orElseGet(() -> BaseResponse.fail(HttpStatus.BAD_REQUEST, "payment.not_found"));
    }

    @Override
    public BaseResponse<PaymentResponse> getMyPaymentByOrderCode(Long orderCode) {
        String userId = currentUserId();
        if (!StringUtils.hasText(userId) || orderCode == null) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "payment.not_found");
        }
        return paymentRepository.findFirstByUserIdAndProviderOrderCodeAndDeletedAtIsNull(userId, orderCode)
                .map(entity -> BaseResponse.success(HttpStatus.OK, toResponse(entity)))
                .orElseGet(() -> BaseResponse.fail(HttpStatus.BAD_REQUEST, "payment.not_found"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<PaymentResponse> syncMyPaymentByOrderCode(Long orderCode) {
        String userId = currentUserId();
        if (!StringUtils.hasText(userId) || orderCode == null) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "payment.not_found");
        }
        PaymentEntity payment = paymentRepository
                .findFirstByUserIdAndProviderOrderCodeAndDeletedAtIsNull(userId, orderCode)
                .orElse(null);
        if (payment == null) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "payment.not_found");
        }
        payment = expireIfNeeded(payment);
        if (payment.getStatus() != PaymentStatus.PAID) {
            try {
                PayosPaymentLinkResponse providerPayment = payosClient.getPaymentLink(orderCode);
                if (providerPayment != null && "PAID".equalsIgnoreCase(providerPayment.getStatus())) {
                    payment = markPaid(payment, "return-sync");
                }
            } catch (IllegalStateException exception) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, exception.getMessage());
            }
        }
        if (payment.getStatus() == PaymentStatus.PAID && !provisionPaidCourse(payment)) {
            return BaseResponse.fail(HttpStatus.SERVICE_UNAVAILABLE, "payment.enrollment_pending");
        }
        return BaseResponse.success(HttpStatus.OK, toResponse(payment));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<PaymentResponse> cancelMyPaymentByOrderCode(Long orderCode) {
        String userId = currentUserId();
        if (!StringUtils.hasText(userId) || orderCode == null) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "payment.not_found");
        }

        PaymentEntity payment = paymentRepository
                .findFirstByUserIdAndProviderOrderCodeAndDeletedAtIsNull(userId, orderCode)
                .orElse(null);
        if (payment == null) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "payment.not_found");
        }

        payment = expireIfNeeded(payment);
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            return BaseResponse.success(HttpStatus.OK, toResponse(payment));
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "payment.cannot_cancel");
        }

        if (PROVIDER_PAYOS.equalsIgnoreCase(payment.getProvider())) {
            payosClient.cancelPaymentLink(orderCode, "User requested cancellation");
        }
        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setRawWebhook("cancelled-by-user");
        return BaseResponse.success(HttpStatus.OK, toResponse(paymentRepository.save(payment)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<PaymentResponse> handlePayosWebhook(PayosWebhookRequest request) {
        if (!payosClient.verifyWebhook(request)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "payos.invalid_webhook");
        }

        Long orderCode = asLong(request.getData().get("orderCode"));
        if (orderCode == null) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "payos.invalid_order_code");
        }

        PaymentEntity payment = paymentRepository.findFirstByProviderOrderCodeAndDeletedAtIsNull(orderCode)
                .orElse(null);
        if (payment == null) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "payment.not_found");
        }
        payment = expireIfNeeded(payment);
        if (payment.getStatus() == PaymentStatus.EXPIRED) {
            // Return 200 to PayOS so a late webhook is not retried indefinitely.
            return BaseResponse.success(HttpStatus.OK, toResponse(payment));
        }
        if (payment.getStatus() == PaymentStatus.PAID) {
            provisionPaidCourse(payment);
            return BaseResponse.success(HttpStatus.OK, toResponse(payment));
        }

        Long amountValue = asLong(request.getData().get("amount"));
        if (amountValue == null) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "payment.amount_invalid");
        }
        BigDecimal paidAmount = BigDecimal.valueOf(amountValue);
        if (paidAmount.compareTo(payment.getAmount()) != 0) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setRawWebhook(rawJson(request));
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "payment.amount_mismatch");
        }

        payment.setProviderTransactionId(resolveTransactionId(request, orderCode));
        payment.setRawWebhook(rawJson(request));
        PaymentEntity saved = markPaid(payment, "webhook");
        provisionPaidCourse(saved);

        return BaseResponse.success(HttpStatus.OK, toResponse(saved));
    }

    private PaymentEntity markPaid(PaymentEntity payment, String source) {
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(payment.getPaidAt() == null ? LocalDateTime.now() : payment.getPaidAt());
        payment.setInvoiceIssuedAt(payment.getInvoiceIssuedAt() == null ? LocalDateTime.now() : payment.getInvoiceIssuedAt());
        if (!StringUtils.hasText(payment.getRawWebhook())) {
            payment.setRawWebhook(source);
        }
        return paymentRepository.save(payment);
    }

    private PaymentEntity expireIfNeeded(PaymentEntity payment) {
        if (payment.getStatus() != PaymentStatus.PENDING || payment.getCreatedDate() == null) {
            return payment;
        }
        if (payment.getCreatedDate().plusHours(PAYMENT_EXPIRY_HOURS).isAfter(LocalDateTime.now())) {
            return payment;
        }
        payment.setStatus(PaymentStatus.EXPIRED);
        payment.setRawWebhook("expired-after-24-hours");
        return paymentRepository.save(payment);
    }

    private int expirePendingPayments() {
        LocalDateTime now = LocalDateTime.now();
        return paymentRepository.expirePendingPayments(
                PaymentStatus.PENDING,
                PaymentStatus.EXPIRED,
                now.minusHours(PAYMENT_EXPIRY_HOURS),
                now,
                "expired-after-24-hours"
        );
    }

    private boolean provisionPaidCourse(PaymentEntity payment) {
        boolean enrolled = ensureEnrollment(payment);
        ensureInvoice(payment);
        return enrolled;
    }

    private boolean ensureEnrollment(PaymentEntity payment) {
        try {
            BaseResponse<Object> response = learningClient.enrollPaidCourse(
                    payment.getUserId(), payment.getCourseId());
            boolean success = response != null
                    && response.getStatus() != null
                    && response.getStatus().is2xxSuccessful()
                    && response.getData() != null;
            if (!success) {
                log.warn("Paid payment {} could not enroll user {} in course {}: {}",
                        payment.getId(), payment.getUserId(), payment.getCourseId(),
                        response == null ? "empty response" : response.getMessage());
            }
            return success;
        } catch (Exception exception) {
            log.warn("Paid payment {} enrollment call failed: {}", payment.getId(), exception.getMessage());
            return false;
        }
    }

    private void ensureInvoice(PaymentEntity payment) {
        try {
            createInvoice(payment);
        } catch (Exception exception) {
            // Invoice creation is retried on every payment sync and must not block course access.
            log.warn("Paid payment {} invoice creation failed: {}", payment.getId(), exception.getMessage());
        }
    }

    private PaymentEntity createFreePayment(String userId, String courseId) {
        Optional<PaymentEntity> paid = paymentRepository
                .findFirstByUserIdAndCourseIdAndStatusAndDeletedAtIsNullOrderByCreatedDateDesc(
                        userId, courseId, PaymentStatus.PAID);
        if (paid.isPresent()) {
            return paid.get();
        }

        Long orderCode = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();
        PaymentEntity payment = new PaymentEntity();
        payment.setUserId(userId);
        payment.setCourseId(courseId);
        payment.setAmount(BigDecimal.ZERO);
        payment.setProvider(PROVIDER_FREE);
        payment.setProviderOrderCode(orderCode);
        payment.setInvoiceCode(invoiceCode(orderCode));
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(now);
        payment.setInvoiceIssuedAt(now);
        return paymentRepository.save(payment);
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }

    private PaymentResponse toResponse(PaymentEntity entity) {
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
        response.setDisplayDate(entity.getPaidAt() == null ? entity.getCreatedDate() : entity.getPaidAt());
        return response;
    }

    private void createInvoice(PaymentEntity payment) {
        InvoiceCreateRequest invoice = new InvoiceCreateRequest();
        invoice.setPaymentId(payment.getId());
        invoice.setInvoiceCode(payment.getInvoiceCode());
        invoice.setUserId(payment.getUserId());
        invoice.setCourseId(payment.getCourseId());
        invoice.setAmount(payment.getAmount());
        invoice.setProvider(payment.getProvider());
        invoice.setProviderTransactionId(payment.getProviderTransactionId());
        invoice.setStatus(payment.getStatus().name());
        invoice.setIssuedAt(payment.getInvoiceIssuedAt());
        invoice.setPaidAt(payment.getPaidAt());
        invoiceClient.createOrGet(invoice, internalServiceKey);
    }

    private String invoiceCode(Long orderCode) {
        return "INV-" + LocalDateTime.now().format(INVOICE_DATE_FORMAT) + "-" + orderCode;
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer toPayosAmount(BigDecimal amount) {
        try {
            return amount.setScale(0, RoundingMode.UNNECESSARY).intValueExact();
        } catch (ArithmeticException ex) {
            return null;
        }
    }

    private String resolveTransactionId(PayosWebhookRequest request, Long orderCode) {
        Object reference = request.getData().get("reference");
        if (reference != null && StringUtils.hasText(reference.toString())) {
            return reference.toString();
        }
        Object paymentLinkId = request.getData().get("paymentLinkId");
        if (paymentLinkId != null && StringUtils.hasText(paymentLinkId.toString())) {
            return paymentLinkId.toString();
        }
        return "PAYOS-" + orderCode;
    }

    private String rawJson(PayosWebhookRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (Exception ex) {
            return null;
        }
    }
}
