package vn.com.atomi.charge.chat.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.chat.model.dto.TokenIntrospectionDto;
import vn.com.atomi.charge.chat.model.dto.UserSummaryDto;
import vn.com.atomi.charge.chat.model.request.TokenIntrospectionRequest;

@FeignClient(name = "lms-authn-service")
public interface AuthnClient {

    @PostMapping("/api/v1/auth/introspect")
    BaseResponse<TokenIntrospectionDto> introspect(@RequestBody TokenIntrospectionRequest request);

    @GetMapping("/internal/v1/AuthnUser/{userId}/info")
    BaseResponse<UserSummaryDto> getUser(@PathVariable String userId);
}
