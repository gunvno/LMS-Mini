package vn.com.atomi.charge.payment.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.payment.model.request.CreateCoursePaymentRequest;
import vn.com.atomi.charge.payment.model.request.PayosWebhookRequest;
import vn.com.atomi.charge.payment.model.response.PaymentResponse;

public interface PaymentService {
    BaseResponse<PaymentResponse> createCoursePayment(BaseRequest<CreateCoursePaymentRequest> request);
    BaseResponse<Page<PaymentResponse>> getMyPayments(Pageable pageable);
    BaseResponse<Page<PaymentResponse>> getAllPayments(Pageable pageable);
    BaseResponse<PaymentResponse> getPayment(String id);
    BaseResponse<PaymentResponse> getMyPaymentByOrderCode(Long orderCode);
    BaseResponse<PaymentResponse> syncMyPaymentByOrderCode(Long orderCode);
    BaseResponse<PaymentResponse> cancelMyPaymentByOrderCode(Long orderCode);
    BaseResponse<PaymentResponse> handlePayosWebhook(PayosWebhookRequest request);
}
