package vn.com.atomi.charge.gateway.service.rest;

import vn.com.atomi.charge.gateway.dto.AuthnUserInfoDto;
import vn.com.atomi.charge.gateway.dto.BaseResponseDto;
import vn.com.atomi.charge.gateway.dto.IntrospectRequestDto;
import vn.com.atomi.charge.gateway.dto.IntrospectResponseDto;
import vn.com.atomi.charge.gateway.dto.VerifySignRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "lms-authn-service", url = "${config.api.authn}")
public interface AuthnClient {

  @PostMapping(value = "/api/v1/auth/introspect")
  BaseResponseDto<IntrospectResponseDto> introspect(@RequestBody IntrospectRequestDto request);

  @PostMapping(value = "/api/v1/auth/me")
  BaseResponseDto<AuthnUserInfoDto> getUserInfo(@RequestBody String token);

  @PostMapping(value = "/internal/v1/verify/signature")
  ResponseEntity<Boolean> verifySign(@RequestBody VerifySignRequestDto request);
}
