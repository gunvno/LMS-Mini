package vn.com.atomi.charge.course.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.atomi.charge.base.controller.BaseController;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.course.model.dto.CourseDto;
import vn.com.atomi.charge.course.model.request.RejectCourseRequest;
import vn.com.atomi.charge.course.service.interfaces.CourseService;
import vn.com.atomi.charge.course.service.interfaces.ImageService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Courses", description = "CRUD APIs for courses")
public class CourseController extends BaseController<CourseService, CourseDto> {

    private final ImageService imageService;

    public CourseController(ImageService imageService) {
        this.imageService = imageService;
    }

    @Override
    @GetMapping
    @PreAuthorize("hasAuthority('COURSE_VIEW')")
    public ResponseEntity<?> getAll(@RequestParam Map<String, String> params, Pageable pageable) {
        BaseResponse<Page<CourseDto>> dtos = service.getAll(params, pageable);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/published")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getPublishedCourses(Pageable pageable) {
        return ResponseEntity.ok(service.getPublishedCourses(pageable));
    }

    @Override
    @GetMapping(value = {"/{id}"})
    @PreAuthorize("hasAuthority('COURSE_VIEW')")
    public ResponseEntity<?> getDetails(@PathVariable("id") String id) {
        BaseResponse<CourseDto> dto = service.getDetails(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/published")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getPublishedCourseDetails(@PathVariable String id) {
        return ResponseEntity.ok(service.getPublishedCourseDetails(id));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    @Validated(BaseDto.Create.class)
    public ResponseEntity<?> create(@RequestBody @Valid BaseRequest<CourseDto> dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @Override
    @PostMapping(value = {"/{id}"})
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    @Validated(BaseDto.Update.class)
    public ResponseEntity<?> update(@RequestBody @Valid BaseRequest<CourseDto> dto,
                                    @PathVariable String id) {
        dto.getData().setId(id);
        return ResponseEntity.ok(service.update(dto));
    }

    @Override
    @DeleteMapping(value = {"/{id}"})
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @Override
    @DeleteMapping
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    public ResponseEntity<?> deleteMany(@RequestBody List<String> ids) {
        return ResponseEntity.ok(service.delete(ids));
    }

    @PostMapping("/{id}/submit-review")
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    public ResponseEntity<?> submitReview(@PathVariable String id) {
        return ResponseEntity.ok(service.submitReview(id));
    }
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('COURSE_REVIEW')")
    public ResponseEntity<?> approveCourse(@PathVariable String id) {
        return ResponseEntity.ok(service.approveCourse(id));
    }
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('COURSE_REVIEW')")
    public ResponseEntity<?> rejectCourse(@RequestBody RejectCourseRequest request) {
        return ResponseEntity.ok(service.rejectCourse(request));
    }
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    public ResponseEntity<?> archiveCourse(@PathVariable String id) {
        return ResponseEntity.ok(service.archiveCourse(id));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('IMAGE_MANAGE')")
    public ResponseEntity<?> uploadCourseImage(@PathVariable String id,
                                               @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(imageService.uploadCourseImage(id, file));
    }

    @GetMapping("/{id}/images")
    @PreAuthorize("hasAuthority('IMAGE_VIEW')")
    public ResponseEntity<?> getCourseImages(@PathVariable String id) {
        return ResponseEntity.ok(imageService.getCourseImages(id));
    }

    @GetMapping("/{id}/images/primary/view")
    @PreAuthorize("hasAuthority('IMAGE_VIEW')")
    public ResponseEntity<byte[]> viewPrimaryCourseImage(@PathVariable String id) {
        return imageService.viewPrimaryCourseImage(id);
    }
}
