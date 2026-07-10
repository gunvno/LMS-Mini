package vn.com.atomi.charge.learning.repository.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.learning.model.request.NoticeRequest;

@FeignClient(name = "lms-notice-service")
public interface NoticeClient {
    @PostMapping("/internal/v1/notices/send-user")
    void sendUser(@RequestBody BaseRequest<NoticeRequest> request);
    @PostMapping("/internal/v1/notices/send-role")
    void sendRole(@RequestBody BaseRequest<NoticeRequest> request);
}
