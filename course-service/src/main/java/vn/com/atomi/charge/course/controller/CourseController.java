package vn.com.atomi.charge.course.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.atomi.charge.base.controller.BaseController;
import vn.com.atomi.charge.course.model.dto.CourseDto;
import vn.com.atomi.charge.course.model.request.RejectCourseRequest;
import vn.com.atomi.charge.course.service.interfaces.CourseService;
import vn.com.atomi.charge.course.service.interfaces.ImageService;

@RestController
@RequestMapping("/courses")
@Tag(name = "Courses", description = "CRUD APIs for courses")
public class CourseController extends BaseController<CourseService, CourseDto> {

    private final ImageService imageService;

    public CourseController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/{id}/submit-review")
    public ResponseEntity<?> submitReview(@PathVariable String id) {
        return ResponseEntity.ok(service.submitReview(id));
    }
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveCourse(@PathVariable String id) {
        return ResponseEntity.ok(service.approveCourse(id));
    }
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectCourse(@RequestBody RejectCourseRequest request) {
        return ResponseEntity.ok(service.rejectCourse(request));
    }
    @PostMapping("/{id}/archive")
    public ResponseEntity<?> archiveCourse(@PathVariable String id) {
        return ResponseEntity.ok(service.archiveCourse(id));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadCourseImage(@PathVariable String id,
                                               @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(imageService.uploadCourseImage(id, file));
    }

}
