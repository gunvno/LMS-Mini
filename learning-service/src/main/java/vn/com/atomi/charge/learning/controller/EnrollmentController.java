package vn.com.atomi.charge.learning.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.learning.model.dto.EnrollmentDto;
import vn.com.atomi.charge.learning.service.interfaces.EnrollmentService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Enrollments", description = "APIs for course enrollment")
@PreAuthorize("hasAuthority('ENROLLMENT_VIEW')")
public class EnrollmentController {

    private final EnrollmentService service;

    public EnrollmentController(EnrollmentService service) {
        this.service = service;
    }

    @PostMapping("/courses/{courseId}/enroll")
    @PreAuthorize("hasAuthority('ENROLLMENT_ENROLL')")
    public ResponseEntity<?> enrollCourse(@PathVariable String courseId,
                                          @RequestBody(required = false) BaseRequest<EnrollmentDto> request) {
        return ResponseEntity.ok(service.enrollCourse(request, courseId));
    }
    @GetMapping("/my-courses")
    @PreAuthorize("hasAuthority('ENROLLMENT_VIEW')")
    public ResponseEntity<?> getMyCourse(Pageable pageable) {
        return ResponseEntity.ok(service.getMyEnroll(pageable));
    }
    @PostMapping("/enrollments/{id}/complete")
    @PreAuthorize("hasAuthority('LEARNING_PROGRESS_UPDATE')")
    public ResponseEntity<?> finishCourse(@PathVariable String id){
        return ResponseEntity.ok(service.finishCourse(id));
    }
}
