package vn.com.atomi.charge.quiz.repository.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.quiz.model.dto.EnrollmentDto;

@FeignClient(name = "lms-learning-service")
public interface LearningClient {
    @GetMapping("/internal/v1/enrollment/{courseId}")
    BaseResponse<EnrollmentDto> findEnrollment(@PathVariable("courseId") String courseId);
}
