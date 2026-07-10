package vn.com.atomi.charge.payment.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.payment.model.dto.CreateCoursePaymentRequest;
import vn.com.atomi.charge.payment.model.dto.PaymentDto;
import vn.com.atomi.charge.payment.model.dto.PayosWebhookRequest;

public interface PaymentService {
    BaseResponse<PaymentDto> createCoursePayment(BaseRequest<CreateCoursePaymentRequest> request);
    BaseResponse<Page<PaymentDto>> getMyPayments(Pageable pageable);
    BaseResponse<Page<PaymentDto>> getAllPayments(Pageable pageable);
    BaseResponse<PaymentDto> getPayment(String id);
    BaseResponse<PaymentDto> getMyPaymentByOrderCode(Long orderCode);
    BaseResponse<PaymentDto> syncMyPaymentByOrderCode(Long orderCode);
    BaseResponse<PaymentDto> handlePayosWebhook(PayosWebhookRequest request);
}
