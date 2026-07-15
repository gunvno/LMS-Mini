package vn.com.atomi.charge.gateway.service.rest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import vn.com.atomi.charge.gateway.dto.AuthnUserInfoDto;
import vn.com.atomi.charge.gateway.dto.BaseResponseDto;
import vn.com.atomi.charge.gateway.dto.IntrospectRequestDto;
import vn.com.atomi.charge.gateway.dto.IntrospectResponseDto;

@FeignClient(name = "lms-authn-service", url = "${config.api.authn}")
public interface AuthnClient {

  @PostMapping(value = "/api/v1/auth/introspect")
  BaseResponseDto<IntrospectResponseDto> introspect(
      @RequestHeader("X-Client-Portal") String portal,
      @RequestBody IntrospectRequestDto request);

  @PostMapping(value = "/api/v1/auth/me")
  BaseResponseDto<AuthnUserInfoDto> getUserInfo(
      @RequestHeader("X-Client-Portal") String portal,
      @RequestBody String token);
}
