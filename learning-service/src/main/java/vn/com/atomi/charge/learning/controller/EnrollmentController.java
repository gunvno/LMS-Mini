package vn.com.atomi.charge.learning.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.com.atomi.charge.base.controller.BaseController;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.learning.model.dto.EnrollmentDto;
import vn.com.atomi.charge.learning.service.interfaces.EnrollmentService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Enrollments", description = "APIs for course enrollment")
public class EnrollmentController extends BaseController<EnrollmentService, EnrollmentDto> {

    @PostMapping("/courses/{courseId}/enroll")
    @Validated(BaseDto.Create.class)
    public ResponseEntity<?> enrollCourse(@PathVariable String courseId,
                                          @RequestBody @Valid BaseRequest<EnrollmentDto> request) {
        return ResponseEntity.ok(service.enrollCourse(request, courseId));
    }
    @GetMapping("/my-courses")
    public ResponseEntity<?> getMyCourse(Pageable pageable) {
        return ResponseEntity.ok(service.getMyEnroll(pageable));
    }
    @PostMapping("/enrollments/{id}/complete")
    public ResponseEntity<?> finishCourse(String id){
        return ResponseEntity.ok(service.finishCourse(id));
    }
}
