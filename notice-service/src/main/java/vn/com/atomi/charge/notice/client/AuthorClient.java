package vn.com.atomi.charge.notice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.com.atomi.charge.base.model.response.BaseResponse;

import java.util.List;

@FeignClient(name = "lms-author-service")
public interface AuthorClient {

    @GetMapping("/internal/v1/roles/{roleCode}/users")
    BaseResponse<List<String>> getUsersByRole(@PathVariable("roleCode") String roleCode);
}
