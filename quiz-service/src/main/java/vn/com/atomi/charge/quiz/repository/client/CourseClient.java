package vn.com.atomi.charge.quiz.repository.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "lms-course-service")
public interface CourseClient {

    @GetMapping("/internal/v1/courses/{courseId}/instructors/{userId}/owner")
    Boolean isInstructorOwner(@PathVariable("courseId") String courseId,
                              @PathVariable("userId") String userId);

    @GetMapping("/internal/v1/courses/instructors/{userId}/ids")
    List<String> getInstructorCourseIds(@PathVariable("userId") String userId);
}
