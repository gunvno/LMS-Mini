package vn.com.atomi.charge.course.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.course.service.interfaces.CourseService;

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

}
