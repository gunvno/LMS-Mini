package vn.com.atomi.charge.learning.repository.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.learning.model.dto.AuthnUserDto;

@FeignClient(name = "lms-authn-service")
public interface AuthnClient {
    @PostMapping("/internal/v1/AuthnUser/{id}/check")
    Boolean checkUser(@PathVariable String id);
    @GetMapping("/internal/v1/AuthUser/{userId}/info")
    BaseResponse<AuthnUserDto> getUserById(@PathVariable String userId);
}
