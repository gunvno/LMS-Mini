package vn.com.atomi.charge.authn.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import vn.com.atomi.charge.base.model.response.BaseResponse;

import java.util.List;

@FeignClient(name = "lms-author-service")
public interface AuthorClient {
    @PostMapping("/internal/v1/users/{userId}/roles/student")
    void assignStudentRole(@PathVariable String userId);

    @GetMapping("/internal/v1/users/{userId}/roles")
    BaseResponse<List<String>> getUserRoles(@PathVariable String userId);
}
