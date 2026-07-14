package vn.com.atomi.charge.billing.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.billing.model.request.CreateCoursePaymentRequest;
import vn.com.atomi.charge.billing.model.request.PayosWebhookRequest;
import vn.com.atomi.charge.billing.service.interfaces.PaymentService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Payments", description = "Course payment APIs")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/course-payments")
    @PreAuthorize("hasAuthority('PAYMENT_CREATE')")
    public ResponseEntity<?> createCoursePayment(@RequestBody BaseRequest<CreateCoursePaymentRequest> request) {
        return ResponseEntity.ok(paymentService.createCoursePayment(request));
    }

    @GetMapping("/payments/me")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    public ResponseEntity<?> getMyPayments(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getMyPayments(pageable));
    }

    @GetMapping("/admin/payments")
    @PreAuthorize("hasAuthority('PAYMENT_MANAGE')")
    public ResponseEntity<?> getAllPayments(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }

    @GetMapping("/payments/history/me")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    public ResponseEntity<?> getMyPaymentHistory(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getMyPayments(pageable));
    }

    @GetMapping("/payments/{id}")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    public ResponseEntity<?> getPayment(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @GetMapping("/payments/orders/{orderCode}")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    public ResponseEntity<?> getPaymentByPayosOrderCode(@PathVariable Long orderCode) {
        return ResponseEntity.ok(paymentService.getMyPaymentByOrderCode(orderCode));
    }

    @PostMapping("/payments/orders/{orderCode}/sync")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    public ResponseEntity<?> syncPaymentByPayosOrderCode(@PathVariable Long orderCode) {
        return ResponseEntity.ok(paymentService.syncMyPaymentByOrderCode(orderCode));
    }

    @PostMapping("/payments/orders/{orderCode}/cancel")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    public ResponseEntity<?> cancelPaymentByPayosOrderCode(@PathVariable Long orderCode) {
        return ResponseEntity.ok(paymentService.cancelMyPaymentByOrderCode(orderCode));
    }

    @GetMapping("/payments/payos/webhook")
    public ResponseEntity<?> payosWebhookReady() {
        return ResponseEntity.ok("PayOS webhook is ready");
    }

    @PostMapping({"/payments/payos", "/payments/payos/webhook"})
    public ResponseEntity<?> payosWebhook(@RequestBody PayosWebhookRequest request) {
        return ResponseEntity.ok(paymentService.handlePayosWebhook(request));
    }
}
