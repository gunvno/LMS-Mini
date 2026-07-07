package vn.com.atomi.charge.learning.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.base.controller.BaseController;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.learning.model.dto.LearningProgressDto;
import vn.com.atomi.charge.learning.service.interfaces.LearningProgressService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "LearningProgress", description = "APIs for course enrollment")
public class LearningProgressController extends BaseController<LearningProgressService, LearningProgressDto> {
    @PostMapping("/lessons/{lessonId}/start")
    public BaseResponse<LearningProgressDto> startLesson(@PathVariable String lessonId){
        return service.startLesson(lessonId);
    }
    @PostMapping("/lessons/{lessonId}/complete")
    public BaseResponse<LearningProgressDto> finishLesson(@PathVariable String lessonId){
        return service.finishLesson(lessonId);
    }
}
