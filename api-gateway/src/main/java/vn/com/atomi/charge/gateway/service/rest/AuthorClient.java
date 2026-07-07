package vn.com.atomi.charge.gateway.service.rest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.com.atomi.charge.gateway.dto.BaseResponseDto;

import java.util.List;

@FeignClient(name = "lms-author-service", url = "${config.api.author}")
public interface AuthorClient {

  @GetMapping(value = "/internal/v1/users/{userId}/permissions")
  BaseResponseDto<List<String>> getUserPermissions(@PathVariable String userId);
}
