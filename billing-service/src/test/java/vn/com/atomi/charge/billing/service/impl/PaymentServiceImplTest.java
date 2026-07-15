package vn.com.atomi.charge.billing.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.billing.client.CourseClient;
import vn.com.atomi.charge.billing.client.LearningClient;
import vn.com.atomi.charge.billing.client.PayosClient;
import vn.com.atomi.charge.billing.mapper.PaymentMapper;
import vn.com.atomi.charge.billing.model.entity.PaymentEntity;
import vn.com.atomi.charge.billing.model.enums.PaymentStatus;
import vn.com.atomi.charge.billing.model.request.PayosWebhookRequest;
import vn.com.atomi.charge.billing.model.response.PaymentResponse;
import vn.com.atomi.charge.billing.model.response.PayosPaymentLinkResponse;
import vn.com.atomi.charge.billing.repository.PaymentRepository;
import vn.com.atomi.charge.billing.service.interfaces.InvoiceService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceImplTest {

    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final CourseClient courseClient = mock(CourseClient.class);
    private final InvoiceService invoiceService = mock(InvoiceService.class);
    private final LearningClient learningClient = mock(LearningClient.class);
    private final PayosClient payosClient = mock(PayosClient.class);
    private final PaymentServiceImpl service = new PaymentServiceImpl(
            paymentRepository,
            courseClient,
            invoiceService,
            learningClient,
            payosClient,
            new ObjectMapper(),
            new PaymentMapper());

    @BeforeEach
    void authenticateUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user-1", null));
        when(paymentRepository.save(any(PaymentEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(learningClient.enrollPaidCourse("user-1", "course-1"))
                .thenReturn(BaseResponse.success(HttpStatus.OK, new Object()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cancelRejectsAndCompletesPaymentWhenProviderAlreadyPaid() {
        PaymentEntity payment = pendingPayment();
        when(paymentRepository.findForUpdateByUserIdAndOrderCode("user-1", 123L))
                .thenReturn(Optional.of(payment));
        when(payosClient.getPaymentLink(123L)).thenReturn(providerStatus("PAID"));

        BaseResponse<PaymentResponse> response = service.cancelMyPaymentByOrderCode(123L);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getMessage()).isEqualTo("payment.already_paid");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaidAt()).isNotNull();
        verify(payosClient, never()).cancelPaymentLink(any(), any());
    }

    @Test
    void cancelOnlyPersistsCancelledAfterProviderConfirmsIt() {
        PaymentEntity payment = pendingPayment();
        when(paymentRepository.findForUpdateByUserIdAndOrderCode("user-1", 123L))
                .thenReturn(Optional.of(payment));
        when(payosClient.getPaymentLink(123L)).thenReturn(providerStatus("PENDING"));
        when(payosClient.cancelPaymentLink(123L, "User requested cancellation"))
                .thenReturn(providerStatus("CANCELLED"));

        BaseResponse<PaymentResponse> response = service.cancelMyPaymentByOrderCode(123L);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getData().getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getRawWebhook()).isEqualTo("cancelled-by-user");
    }

    @Test
    void cancelHandlesPaymentThatWinsTheRaceAfterPrecheck() {
        PaymentEntity payment = pendingPayment();
        when(paymentRepository.findForUpdateByUserIdAndOrderCode("user-1", 123L))
                .thenReturn(Optional.of(payment));
        when(payosClient.getPaymentLink(123L)).thenReturn(providerStatus("PENDING"));
        when(payosClient.cancelPaymentLink(123L, "User requested cancellation"))
                .thenReturn(providerStatus("PAID"));

        BaseResponse<PaymentResponse> response = service.cancelMyPaymentByOrderCode(123L);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void unsuccessfulSignedWebhookNeverMarksPaymentPaid() {
        PaymentEntity payment = pendingPayment();
        PayosWebhookRequest request = webhook(false);
        when(payosClient.verifyWebhook(request)).thenReturn(true);
        when(paymentRepository.findForUpdateByOrderCode(123L)).thenReturn(Optional.of(payment));

        service.handlePayosWebhook(request);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void successfulLateWebhookRecoversExpiredPayment() {
        PaymentEntity payment = pendingPayment();
        payment.setStatus(PaymentStatus.EXPIRED);
        PayosWebhookRequest request = webhook(true);
        when(payosClient.verifyWebhook(request)).thenReturn(true);
        when(paymentRepository.findForUpdateByOrderCode(123L)).thenReturn(Optional.of(payment));

        service.handlePayosWebhook(request);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaidAt()).isNotNull();
    }

    private PaymentEntity pendingPayment() {
        PaymentEntity payment = new PaymentEntity();
        payment.setUserId("user-1");
        payment.setCourseId("course-1");
        payment.setProvider("PAYOS");
        payment.setProviderOrderCode(123L);
        payment.setAmount(BigDecimal.valueOf(100_000));
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }

    private PayosPaymentLinkResponse providerStatus(String status) {
        PayosPaymentLinkResponse response = new PayosPaymentLinkResponse();
        response.setStatus(status);
        return response;
    }

    private PayosWebhookRequest webhook(boolean success) {
        Map<String, Object> data = new HashMap<>();
        data.put("orderCode", 123L);
        data.put("amount", 100_000L);
        data.put("reference", "provider-reference");
        PayosWebhookRequest request = new PayosWebhookRequest();
        request.setCode(success ? "00" : "01");
        request.setSuccess(success);
        request.setData(data);
        request.setSignature("valid-signature");
        return request;
    }
}
