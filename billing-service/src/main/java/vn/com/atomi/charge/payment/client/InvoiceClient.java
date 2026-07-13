package vn.com.atomi.charge.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.payment.model.request.InvoiceCreateRequest;

@FeignClient(name = "lms-invoice-service")
public interface InvoiceClient {
    @PostMapping("/api/v1/internal/invoices")
    BaseResponse<Object> createOrGet(@RequestBody InvoiceCreateRequest request,
                                     @RequestHeader("X-Internal-Service-Key") String serviceKey);
}
