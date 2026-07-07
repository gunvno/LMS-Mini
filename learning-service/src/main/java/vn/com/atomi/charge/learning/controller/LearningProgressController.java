package vn.com.atomi.charge.learning.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.learning.model.dto.LearningProgressDto;
import vn.com.atomi.charge.learning.service.interfaces.LearningProgressService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "LearningProgress", description = "APIs for course enrollment")
@PreAuthorize("hasAuthority('LEARNING_PROGRESS_VIEW')")
public class LearningProgressController {

    private final LearningProgressService service;

    public LearningProgressController(LearningProgressService service) {
        this.service = service;
    }

    @PostMapping("/lessons/{lessonId}/start")
    @PreAuthorize("hasAuthority('LEARNING_PROGRESS_UPDATE')")
    public BaseResponse<LearningProgressDto> startLesson(@PathVariable String lessonId){
        return service.startLesson(lessonId);
    }
    @PostMapping("/lessons/{lessonId}/complete")
    @PreAuthorize("hasAuthority('LEARNING_PROGRESS_UPDATE')")
    public BaseResponse<LearningProgressDto> finishLesson(@PathVariable String lessonId){
        return service.finishLesson(lessonId);
    }
}
