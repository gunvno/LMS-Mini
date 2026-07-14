package vn.com.atomi.charge.learning.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import vn.com.atomi.charge.learning.model.dto.LessonDto;
import vn.com.atomi.charge.learning.model.dto.CourseNotificationDto;
import vn.com.atomi.charge.base.model.response.BaseResponse;

import java.util.List;

@FeignClient(name = "lms-course-service")
public interface CourseClient {

    @GetMapping("/internal/v1/courses/{id}")
    BaseResponse<CourseNotificationDto> getCourse(@PathVariable("id") String id);

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

    @GetMapping("/internal/v1/courses/instructors/{userId}/ids")
    List<String> getInstructorCourseIds(@PathVariable String userId);
}
