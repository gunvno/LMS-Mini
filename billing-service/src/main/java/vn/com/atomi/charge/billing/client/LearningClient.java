package vn.com.atomi.charge.billing.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import vn.com.atomi.charge.base.model.response.BaseResponse;

@FeignClient(name = "lms-learning-service")
public interface LearningClient {
    @PostMapping("/internal/v1/enrollment/users/{userId}/courses/{courseId}/enroll")
    BaseResponse<Object> enrollPaidCourse(@PathVariable("userId") String userId,
                                          @PathVariable("courseId") String courseId);
}
