package vn.com.atomi.charge.chat.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "lms-learning-service")
public interface LearningClient {

    @GetMapping("/internal/v1/enrollment/users/{userId}/courses/{courseId}/access")
    Boolean hasCourseAccess(@PathVariable String userId, @PathVariable String courseId);
}
