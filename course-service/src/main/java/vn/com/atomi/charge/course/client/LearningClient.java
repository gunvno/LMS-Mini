package vn.com.atomi.charge.course.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "lms-learning-service")
public interface LearningClient {

    @GetMapping("/internal/v1/enrollment/courses/{courseId}/access")
    Boolean hasCourseAccess(@PathVariable("courseId") String courseId);

    @GetMapping("/internal/v1/enrollment/courses/{courseId}/lesson-ids/access")
    List<String> getAccessibleLessonIds(@PathVariable("courseId") String courseId);
}
