package vn.com.atomi.charge.authorization.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "lms-authn-service")
public interface AuthnClient {
    @GetMapping("/internal/v1/AuthnUser/{userId}/check")
    Boolean checkUser(@PathVariable("userId") String userId);
}