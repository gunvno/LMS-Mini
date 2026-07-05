package vn.com.atomi.charge.gateway.service.rest;

import vn.com.atomi.charge.gateway.dto.UserDto;
import vn.com.atomi.charge.gateway.dto.VerifySignRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@FeignClient(name = "ev-authentication-service", url = "${config.api.authn}")
public interface AuthnClient {

  @RequestMapping(method = RequestMethod.GET, value = "/internal/v1/verify/validate-token")
  ResponseEntity<Boolean> validateToken(@RequestParam("token") String token);

  @RequestMapping(method = RequestMethod.GET, value = "/internal/v1/verify/user-info")
  UserDto getUserInfo(@RequestParam("token") String token);

  @PostMapping(value = "/internal/v1/verify/signature")
  ResponseEntity<Boolean> verifySign(@RequestBody VerifySignRequestDto request);
}
