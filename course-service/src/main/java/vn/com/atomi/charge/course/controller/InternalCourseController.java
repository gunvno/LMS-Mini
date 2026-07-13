package vn.com.atomi.charge.course.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import vn.com.atomi.charge.course.service.interfaces.CourseService;
import vn.com.atomi.charge.course.model.dto.CourseDto;
import vn.com.atomi.charge.base.model.response.BaseResponse;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/courses")
public class InternalCourseController {

    private final CourseService courseService;

    public InternalCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping("/{id}/check")
    public Boolean checkCourse(@PathVariable String id) {
        return courseService.checkCourse(id);
    }

    @GetMapping("/{id}/published")
    public Boolean checkPublishedCourse(@PathVariable String id) {
        return courseService.checkPublishedCourse(id);
    }

    @GetMapping("/{id}")
    public BaseResponse<CourseDto> getCourse(@PathVariable String id) {
        return courseService.getDetails(id);
    }

    @GetMapping("/{id}/instructors/{userId}/owner")
    public Boolean isInstructorOwner(@PathVariable String id, @PathVariable String userId) {
        return courseService.isInstructorOwner(id, userId);
    }

    @GetMapping("/instructors/{userId}/ids")
    public List<String> getInstructorCourseIds(@PathVariable String userId) {
        return courseService.getInstructorCourseIds(userId);
    }

    @GetMapping("/published-catalog")
    public BaseResponse<?> getPublishedCatalog(
            @RequestParam(defaultValue = "200") int limit) {
        return courseService.getPublishedCatalog(limit);
    }

}
