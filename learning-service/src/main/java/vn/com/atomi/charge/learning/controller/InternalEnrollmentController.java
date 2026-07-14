package vn.com.atomi.charge.learning.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.learning.service.interfaces.EnrollmentService;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/enrollment")
public class InternalEnrollmentController {
    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping("/{courseId}")
    public ResponseEntity<?> findEnrollment(@PathVariable String courseId){
        return ResponseEntity.ok(enrollmentService.findEnrollmentByCourseIdAndUserId(courseId));
    }

    @PostMapping("/users/{userId}/courses/{courseId}/enroll")
    public ResponseEntity<?> enrollPaidCourse(@PathVariable String userId, @PathVariable String courseId) {
        return ResponseEntity.ok(enrollmentService.enrollCourseForUser(userId, courseId));
    }

    @GetMapping("/courses/{courseId}/access")
    public Boolean hasCourseAccess(@PathVariable String courseId) {
        return enrollmentService.hasCurrentUserCourseAccess(courseId);
    }

    @GetMapping("/users/{userId}/courses/{courseId}/access")
    public Boolean hasUserCourseAccess(@PathVariable String userId, @PathVariable String courseId) {
        return enrollmentService.hasUserCourseAccess(userId, courseId);
    }

    @GetMapping("/course-ids/access")
    public List<String> getAccessibleCourseIds() {
        return enrollmentService.getCurrentUserAccessibleCourseIds();
    }

    @GetMapping("/courses/{courseId}/lesson-ids/access")
    public List<String> getAccessibleLessonIds(@PathVariable String courseId) {
        return enrollmentService.getCurrentUserAccessibleLessonIds(courseId);
    }

    @PostMapping("/courses/{courseId}/complete")
    public ResponseEntity<?> finishCourse(@PathVariable String courseId) {
        return ResponseEntity.ok(enrollmentService.finishCourse(courseId));
    }
}
