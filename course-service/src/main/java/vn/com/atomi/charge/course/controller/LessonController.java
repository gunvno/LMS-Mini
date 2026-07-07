package vn.com.atomi.charge.course.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.course.model.dto.LessonDto;
import vn.com.atomi.charge.course.service.interfaces.LessonService;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Lessons", description = "CRUD APIs for lessons")
public class LessonController {

    private final LessonService service;

    public LessonController(LessonService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/lessons")
    public ResponseEntity<?> getAll(@RequestParam Map<String, String> params, Pageable pageable) {
        BaseResponse<Page<LessonDto>> dtos = service.getAll(params, pageable);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/api/v1/lessons/{id}")
    public ResponseEntity<?> getDetails(@PathVariable("id") String id) {
        BaseResponse<LessonDto> dto = service.getDetails(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/api/v1/lessons")
    @Validated(BaseDto.Create.class)
    public ResponseEntity<?> create(@RequestBody @Valid BaseRequest<LessonDto> dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PostMapping("/api/v1/lessons/{id}")
    @Validated(BaseDto.Update.class)
    public ResponseEntity<?> update(@RequestBody @Valid BaseRequest<LessonDto> dto,
                                    @PathVariable String id) {
        dto.getData().setId(id);
        return ResponseEntity.ok(service.update(dto));
    }

    @DeleteMapping("/api/v1/lessons/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @DeleteMapping("/api/v1/lessons")
    public ResponseEntity<?> deleteMany(@RequestBody List<String> ids) {
        return ResponseEntity.ok(service.delete(ids));
    }

    @GetMapping("/api/v1/courses/{courseId}/lessons")
    public ResponseEntity<?> getLessonsByCourse(@PathVariable String courseId, Pageable pageable) {
        return ResponseEntity.ok(service.getLessonByCourseId(courseId, pageable));
    }

    @PostMapping("/api/v1/courses/{courseId}/lessons")
    @Validated(BaseDto.Create.class)
    public ResponseEntity<?> createLessonByCourse(@PathVariable String courseId,
                                                  @RequestBody @Valid BaseRequest<LessonDto> request) {
        return ResponseEntity.ok(service.createLesson(request, courseId));
    }
}
