package vn.com.atomi.charge.course.controller;

import org.springframework.web.bind.annotation.*;
import vn.com.atomi.charge.course.model.dto.LessonDto;
import vn.com.atomi.charge.course.service.interfaces.CourseService;
import vn.com.atomi.charge.course.service.interfaces.LessonService;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/lesson")
public class InternalLessonController {
    private final LessonService lessonService;

    public InternalLessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping("/{id}/check")
    public Boolean checkLesson( @PathVariable String id) {
        return lessonService.checkLesson(id);
    }
    @GetMapping("/{id}/course")
    public String getCourseByLessonId(@PathVariable String id){
        return  lessonService.getCourseByLessonId(id);
    }
    @GetMapping("/course/{id}")
    public List<LessonDto> getLessonByCourseId(@PathVariable String id){
        return  lessonService.getLessonsByCourseIdNoPage(id);
    }
    @GetMapping("/count/{id}")
    public Double countLessonInCourse(@PathVariable String id){
        return lessonService.countLessonInCourse(id);
    }
}
