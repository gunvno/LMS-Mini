package vn.com.atomi.charge.course.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.com.atomi.charge.base.controller.BaseController;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.course.model.dto.LessonDto;
import vn.com.atomi.charge.course.service.interfaces.LessonService;

@RestController
@RequestMapping
@Tag(name = "Lessons", description = "CRUD APIs for lessons")
public class LessonController extends BaseController<LessonService, LessonDto> {

    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<?> getLessonsByCourse(@PathVariable String courseId, Pageable pageable) {
        return ResponseEntity.ok(service.getLessonByCourseId(courseId, pageable));
    }

    @PostMapping("/courses/{courseId}/lessons")
    @Validated(BaseDto.Create.class)
    public ResponseEntity<?> createLessonByCourse(@PathVariable String courseId,
                                                  @RequestBody @Valid BaseRequest<LessonDto> request) {
        return ResponseEntity.ok(service.createLesson(request, courseId));
    }
}
