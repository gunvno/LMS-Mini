package vn.com.atomi.charge.learning.repository.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import vn.com.atomi.charge.learning.model.dto.LessonDto;

import java.util.List;

@FeignClient(name = "lms-course-service")
public interface CourseClient {

    @PostMapping("/internal/v1/courses/{id}/check")
    Boolean existsCourseById(@PathVariable("id") String id);

    @GetMapping("/internal/v1/courses/{id}/published")
    Boolean existsPublishedCourseById(@PathVariable("id") String id);

    @PostMapping("/internal/v1/lesson/{id}/check")
    Boolean existsLessonById(@PathVariable("id") String id);
    @GetMapping("/internal/v1/lesson/{id}/course")
    String getCourseByLessonId(@PathVariable String id);
    @GetMapping("/internal/v1/lesson/course/{id}")
    List<LessonDto> getLessonByCourseId(@PathVariable String id);
    @GetMapping("/internal/v1/lesson/count/{id}")
    Double countLessonInCourse(@PathVariable String id);
}
