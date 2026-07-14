package vn.com.atomi.charge.quiz.repository.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.quiz.model.dto.EnrollmentDto;

import java.util.List;

@FeignClient(name = "lms-learning-service")
public interface LearningClient {
    @GetMapping("/internal/v1/enrollment/{courseId}")
    BaseResponse<EnrollmentDto> findEnrollment(@PathVariable("courseId") String courseId);

    @GetMapping("/internal/v1/enrollment/courses/{courseId}/access")
    Boolean hasCourseAccess(@PathVariable("courseId") String courseId);

    @GetMapping("/internal/v1/enrollment/course-ids/access")
    List<String> getAccessibleCourseIds();

    @PostMapping("/internal/v1/enrollment/courses/{courseId}/complete")
    BaseResponse<EnrollmentDto> completeCourse(@PathVariable("courseId") String courseId);
}
