package vn.com.atomi.charge.learning.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.learning.service.interfaces.EnrollmentService;

@RestController
@RequestMapping("/internal/v1/enrollment")
public class InternalEnrollmentController {
    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping("/{courseId}")
    public ResponseEntity<?> findEnrollment(@PathVariable String courseId){
        return ResponseEntity.ok(enrollmentService.findEnrollmentByCourseIdAndUserId(courseId));
    }
}
