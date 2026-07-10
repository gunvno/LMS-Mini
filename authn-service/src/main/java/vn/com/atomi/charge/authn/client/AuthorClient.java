package vn.com.atomi.charge.authn.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "lms-author-service")
public interface AuthorClient {
    @PostMapping("/internal/v1/users/{userId}/roles/student")
    void assignStudentRole(@PathVariable String userId);
}
