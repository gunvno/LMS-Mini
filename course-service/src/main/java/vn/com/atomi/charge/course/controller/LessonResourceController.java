package vn.com.atomi.charge.course.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.base.controller.BaseController;
import vn.com.atomi.charge.course.model.dto.LessonResourceDto;
import vn.com.atomi.charge.course.service.interfaces.LessonResourceService;

@RestController
@RequestMapping("/api/v1/lesson-resources")
@Tag(name = "Lesson Resources", description = "CRUD APIs for lesson resources")
public class LessonResourceController
    extends BaseController<LessonResourceService, LessonResourceDto> {
}
